package camundala
package examples.invoice.bpmn

import camundala.api.{CamundaVariable, StartProcessIn}
import camundala.bpmn.*
import camundala.examples.invoice.bpmn.InvoiceApi.{
  assignReviewerUT,
  reviewInvoiceProcess,
  reviewInvoiceUT
}
import camundala.test.CustomTests
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*

// exampleInvoice/GatlingIt/testOnly *BasicSimulation
class BasicSimulation extends Simulation { // 3

  val httpProtocol = http // 4
    .baseUrl("http://localhost:8034/engine-rest") // 5
    .basicAuth("demo", "demo")
    .headers(Map("Content-Type" -> "application/json"))
  //   .doNotTrackHeader("1")
  //   .acceptEncodingHeader("gzip, deflate")

  def test[
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](process: Process[In, Out])(
      elements: ProcessNode*
  ): Seq[ChainBuilder] =
    process.startProcess() +: Nil /*  elements.flatMap {
      case ut: UserTask[? <: Product : Encoder, ?] => ut.getAndComplete()
 //     case st: ServiceTask[?, ?] => st.exec()
  //    case ee: EndEvent => ee.exec()
      case other =>
        throw IllegalArgumentException(
          s"This TestStep is not supported: $other"
        )
    }*/

  extension [
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](process: Process[In, Out])
    def startProcess(): ChainBuilder =
      exec(
        http("request_1") // 8
          .post(s"/process-definition/key/${process.id}/start")
          .body(
            StringBody(
              StartProcessIn(
                CamundaVariable.toCamunda(process.in)
              ).asJson.toString
            )
          )
      )

  extension [
      In <: Product: Encoder,
      Out <: Product: Encoder
  ](userTask: UserTask[In, Out])
    def getAndComplete() =
      Seq(
        http("request_1") // 8
          .post(s"/process-definition/key/${userTask.id}/start")
          .body(
            StringBody(
              StartProcessIn(
                CamundaVariable.toCamunda(userTask.in)
              ).asJson.toString
            )
          )
      )
  setUp( // 11
    scenario("BasicSimulation") // 7
      .exec(
        test(reviewInvoiceProcess)(
          assignReviewerUT,
          reviewInvoiceUT
        ): _*
      )
      .inject(atOnceUsers(1)) // 12
  ).protocols(httpProtocol) // 13
}
