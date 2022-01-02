package camundala.examples.invoice.bpmn

import camundala.api.{CamundaVariable, StartProcessIn}
import camundala.bpmn.*
import camundala.examples.invoice.bpmn.InvoiceApi.{InvoiceReviewed, assignReviewerUT, reviewInvoiceProcess, reviewInvoiceUT}
import camundala.gatling.SimulationRunner
import camundala.test.CustomTests
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*

// exampleInvoice/GatlingIt/testOnly *InvoiceSimulation
class InvoiceSimulation extends SimulationRunner {

  override val serverPort = 8034

  simulate(
    reviewInvoiceProcess.start(),
    assignReviewerUT.getAndComplete(),
    reviewInvoiceUT.getAndComplete(),
    reviewInvoiceProcess.check()
  )
}
