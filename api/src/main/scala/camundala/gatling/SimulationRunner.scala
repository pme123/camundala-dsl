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
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder
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

  implicit def tenantId: Option[String] = None

  def serverPort = 8080
  def maxCheckCount = 2
  def retryDuration: FiniteDuration = 1.second
  def endpoint = s"http://localhost:$serverPort/engine-rest"

  def httpProtocol: HttpProtocolBuilder =
    http // 4
      .baseUrl(endpoint) // 5
      .basicAuth("demo", "demo")
      .headers(Map("Content-Type" -> "application/json"))

  def simulate(requests: (ChainBuilder | Seq[ChainBuilder])*): Unit =
    val requestsFlatten: Seq[ChainBuilder] = requests.flatMap {
      case seq: Seq[ChainBuilder] => seq
      case o: ChainBuilder => Seq(o)
    }
    setUp(
      scenario("BasicSimulation") // 7
        .exec(requestsFlatten)
        .inject(atOnceUsers(1)) // 12
    ).protocols(httpProtocol) // 13

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
          .body(
            StringBody(
              StartProcessIn(
                CamundaVariable.toCamunda(process.in)
              ).asJson.toString
            )
          )//.check(printBody)
          .check(extractJson("$.id", "processInstanceId"))
      )

    def check()(implicit
        tenantId: Option[String]
    ): ChainBuilder =
      exec(
        http(s"Check Process ${process.id}") // 8
          .get("/history/variable-instance?processInstanceIdIn=#{processInstanceId}&deserializeValues=false")
          .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[Seq[CamundaProperty]]) match {
                case Right(value) => checkProps(process.out, value)
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of FormVariables.\n$exc"
              }
            }.is(true)
        )
      )

  end extension

  extension [
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](userTask: UserTask[In, Out])

    def getAndComplete(): Seq[ChainBuilder] = {
      Seq(
        exec(session => session.set("taskId", null)),
        retryOrFail(
          exec(task()).exitHereIfFailed,
          _.attributes.get("taskId").contains(null)
        ),
        exec(checkForm()),
        exec(completeTask())
      )
    }

    private def task(): HttpRequestBuilder =
      http(s"Get Tasks ${userTask.id}")
        .get("/task?processInstanceId=#{processInstanceId}")
        .check(checkMaxCount)
        .check(
          jsonPath("$[*].id").optional
            .saveAs("taskId")
        )

    private def checkForm(): HttpRequestBuilder =
      http(s"Check Form ${userTask.id}")
        .get("/task/#{taskId}/form-variables?deserializeValues=false")
        .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[FormVariables]) match {
                case Right(value) => checkProps(userTask.in, CamundaProperty.from(value))
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of FormVariables.\n$exc"
              }
            }.is(true)
        )

    private def completeTask(): HttpRequestBuilder =
      http(s"Complete Task ${userTask.id}")
        .post(s"/task/$${taskId}/complete")
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
    exec { session =>
      session
        .set("lastStatus", -1)
        .set("retryCount", 0)
    }.doWhile(condition(_)) {
      exec()
        .pause(retryDuration)
        .exec(chainBuilder)
        .exec(session => {
          if (session("lastStatus").asOption[Int].nonEmpty)
            session.set("lastStatus", session("lastStatus").as[Int])
          else
            session
        })
        .exec(session =>
          session.set("retryCount", 1 + session("retryCount").as[Int])
        )
    }.exitHereIfFailed
  }
  private def statusCondition(status: Int*): Session => Boolean = session => {
    println("lastStatus: " + session.apply("lastStatus").as[Int])
    println("retryCount: " + session.apply("retryCount").as[Int])
    val lastStatus = session("lastStatus").as[Int]
    !status.contains(lastStatus)
  }

  private def extractJson(path: String, key: String) =
    jsonPath(path)
      .ofType[String]
      .transform { v =>
        println(s"Extracted $key: $v"); v
      } // save the data
      .saveAs(key)

  private val checkMaxCount = {
    bodyString
      .transformWithSession { (_: String, session: Session) =>
        assert(
          session("retryCount").as[Int] <= maxCheckCount,
          s"The retryCount reached the maximun of $maxCheckCount"
        )
      }
  }

  private val printBody =
    bodyString.transform { b => println(s"Response Body: $b") }

  val printSession: ChainBuilder =
    exec { session =>
      println(s"Session: " + session)
      session
    }

  def checkProps[T <: Product](out: T, result: Seq[CamundaProperty]): Boolean = {
    out.asVarsWithoutEnums()
      .filter(_._2 match
        case None => false
        case _ => true
      )
      .map {
        case (key, value) =>
          result.find(_.key == key)
            .map { obj =>
              obj.value match
                case _: CFile =>
                  println(s">>> Files cannot be tested as its content is _null_ ('$key').")
                  true
                case other =>
                  val matches = obj.value.value == value
                  if (!matches)
                    println(s"!!! The value ' ${obj.value.value}' of $key does not match the result variable '$value'.\n $result")
                  matches
            }
            .getOrElse {
              println(s"!!! $key does not exist in the result variables.\n $result")
              false
            }
      }.forall(_ == true)
  }

end SimulationRunner
