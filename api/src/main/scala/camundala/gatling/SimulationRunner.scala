package camundala
package gatling

import camundala.api.*
import camundala.api.CamundaVariable.CFile
import camundala.bpmn
import camundala.bpmn.{DecisionDmn, InOut, Process, UserTask, *}
import camundala.domain.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import io.gatling.core.Predef.*
import io.gatling.core.structure.{
  ChainBuilder,
  PopulationBuilder,
  ScenarioBuilder
}
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.{HttpRequestBuilder, resolveParamJList}
import laika.api.*
import laika.ast.MessageFilter
import laika.format.*
import laika.markdown.github.GitHubFlavor
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.generic.auto.*
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}
import sttp.tapir.{Endpoint, EndpointInput, EndpointOutput, Schema}

import scala.concurrent.duration.*

trait SimulationRunner extends Simulation:

  // define an implicit tenant if you have one
  implicit def tenantId: Option[String] = None

  // the Camunda Port
  def serverPort: Int = 8080
  // there are Requests that wait until the process is ready - like getTask.
  // the Simulation waits 1 second between the Requests.
  // so with a timeout of 10 sec it will try 10 times (retryDuration = 1.second)
  def timeoutInSec: Int = 10
  def retryDuration: FiniteDuration = 1.second
  // the number of parallel execution of a simulation.
  // for example run the process 3 times (userAtOnce = 3)
  def userAtOnce: Int = 1
  // REST endpoint of Camunda
  def endpoint = s"http://localhost:$serverPort/engine-rest"

  def httpProtocol: HttpProtocolBuilder =
    http
      .baseUrl(endpoint)
      .header("Content-Type", "application/json")

  def ignore(scenarioName: String)(
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): PopulationBuilder =
    scenario(scenarioName)
      .exec { session =>
        println(s">>> Scenario '$scenarioName' is ignored!")
        session
      }
      .inject(atOnceUsers(userAtOnce))

  def preRequests: Seq[ChainBuilder] = Nil

  def processScenario[
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](scenarioName: String)(
      process: Process[In, Out],
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): PopulationBuilder =
    processScenario(scenarioName)(
      (process.start() +:
        flatten(requests)) ++
        process.check(): _*
    )

  def flatten(
      requests: Seq[ChainBuilder | Seq[ChainBuilder]]
  ): Seq[ChainBuilder] =
    requests.flatMap {
      case seq: Seq[ChainBuilder] => seq
      case o: ChainBuilder => Seq(o)
    }

  def processScenario(scenarioName: String)(
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): PopulationBuilder =
    scenario(scenarioName)
      .exec(preRequests ++ flatten(requests))
      .inject(atOnceUsers(userAtOnce))

  def simulate[In <: Product: Encoder, Out <: Product: Encoder](
      examples: Seq[(String, Process[In, Out])]
  ): Unit =
    simulate(
      examples.map { case (k, v) =>
        processScenario(k)(
          v
        )
      }: _*
    )

  def simulate[In <: Product: Encoder, Out <: Product: Encoder](
      example: Process[In, Out]
  ): Unit =
    simulate(
      processScenario("example")(
        example
      )
    )

  def simulate(processScenarios: PopulationBuilder*): Unit =
    setUp(processScenarios: _*)
      .protocols(httpProtocol)
      .assertions(global.failedRequests.count.is(0))

  extension [
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](
      process: Process[In, Out]
  )

    def start()(implicit
        tenantId: Option[String]
    ): ChainBuilder =
      exec(
        http(s"Start Process ${process.id}")
          .post(s"/process-definition/key/${process.id}${tenantId
            .map(id => s"/tenant-id/$id")
            .getOrElse("")}/start")
          .auth()
          .body(
            StringBody(
              StartProcessIn(
                CamundaVariable.toCamunda(process.in)
              ).asJson.toString
            )
          ) //.check(printBody)
          .check(extractJson("$.id", "processInstanceId"))
      ).exitHereIfFailed

    def check(): Seq[ChainBuilder] = {
      Seq(
        exec(_.set("processState", null)),
        retryOrFail(
          exec(checkFinished()).exitHereIfFailed,
          processCondition()
        ),
        exec(checkVars()).exitHereIfFailed
      )
    }

    def checkVars()(implicit
        tenantId: Option[String]
    ): ChainBuilder =
      exec(
        http(s"Check Process ${process.id}") // 8
          .get(
            "/history/variable-instance?processInstanceIdIn=#{processInstanceId}&deserializeValues=false"
          )
          .auth()
          .check(
            bodyString
              .transform { body =>
                parse(body)
                  .flatMap(_.as[Seq[CamundaProperty]]) match {
                  case Right(value) => checkProps(process.out, value)
                  case Left(exc) =>
                    s"\n!!! Problem parsing Result Body to a List of CamundaProperty.\n$exc\n$body"
                }
              }
              .is(true)
          )
      ).exitHereIfFailed

    def checkFinished()(implicit
        tenantId: Option[String]
    ) =
      http(s"Check finished Process ${process.id}")
        .get(s"/history/process-instance/#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(extractJson("$.state", "processState"))

    def switchToCalledProcess(): ChainBuilder =
      exec(session =>
        session.set(
          "processInstanceIdBackup",
          session("processInstanceId").as[String]
        )
      ).exec(
        http(s"Switch to Called Process of ${process.id}")
          .get(s"/process-instance?superProcessInstance=#{processInstanceId}")
          .auth()
          .check(extractJson("$[*].id", "processInstanceId"))
      )

    def switchToMainProcess(): ChainBuilder =
      exec(session =>
        session.set(
          "processInstanceId",
          session("processInstanceIdBackup").as[String]
        )
      )

  end extension

  extension [
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](userTask: UserTask[In, Out])

    def getAndComplete(): Seq[ChainBuilder] = {
      Seq(
        exec(_.set("taskId", null)),
        retryOrFail(
          exec(task()).exitHereIfFailed,
          taskCondition()
        ),
        exec(checkForm()).exitHereIfFailed,
        exec(completeTask()).exitHereIfFailed
      )
    }

    private def task(): HttpRequestBuilder =
      http(s"Get Tasks ${userTask.id}")
        .get("/task?processInstanceId=#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(
          jsonPath("$[*].id").optional
            .saveAs("taskId")
        )

    private def checkForm(): HttpRequestBuilder =
      http(s"Check Form ${userTask.id}")
        .get("/task/#{taskId}/form-variables?deserializeValues=false")
        .auth()
        .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[FormVariables]) match {
                case Right(value) =>
                  checkProps(userTask.in, CamundaProperty.from(value))
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of FormVariables.\n$exc\n$body"
              }
            }
            .is(true)
        )

    private def completeTask(): HttpRequestBuilder =
      http(s"Complete Task ${userTask.id}")
        .post(s"/task/$${taskId}/complete")
        .auth()
        .queryParam("deserializeValues", false)
        .body(
          StringBody(
            CompleteTaskOut(
              CamundaVariable.toCamunda(userTask.out)
            ).asJson.toString
          )
        )

  end extension

  private def retryOrFail(
      chainBuilder: ChainBuilder,
      condition: Session => Boolean = statusCondition(200)
  ) = {
    exec {
      _.set("lastStatus", -1)
        .set("retryCount", 0)
    }.doWhile(condition(_)) {
      exec()
        .pause(retryDuration)
        .exec(chainBuilder)
        .exec { session =>
          if (session("lastStatus").asOption[Int].nonEmpty)
            session.set("lastStatus", session("lastStatus").as[Int])
          else
            session
        }
        .exec(session =>
          session.set("retryCount", 1 + session("retryCount").as[Int])
        )
    }.exitHereIfFailed
  }

  private val checkMaxCount = {
    bodyString
      .transformWithSession { (_: String, session: Session) =>
        assert(
          session("retryCount").as[Int] <= timeoutInSec,
          s"!!! The retryCount reached the maximun of $timeoutInSec"
        )
      }
  }
  def authHeader: HttpRequestBuilder => HttpRequestBuilder = b => b

  extension (builder: HttpRequestBuilder)
    def auth(): HttpRequestBuilder = authHeader(builder)

end SimulationRunner
