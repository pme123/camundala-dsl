package camundala.examples.invoice.bpmn

import camundala.api.{CamundaVariable, StartProcessIn}
import camundala.bpmn.*
import camundala.examples.invoice.bpmn.InvoiceApi.*
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
    processScenario("Review Invoice")(
      reviewInvoiceProcess.start(),
      assignReviewerUT.getAndComplete(),
      reviewInvoiceUT.getAndComplete(),
      reviewInvoiceProcess.check()
    ),
    processScenario("Invoice Receipt")(
      invoiceReceiptProcess.start(),
      approveInvoiceUT.getAndComplete(),
      prepareBankTransferUT.getAndComplete(),
      invoiceReceiptProcess.check()
    ),
    processScenario("Invoice Receipt with Review")(
      invoiceReceiptProcess.start(),
      approveInvoiceUT
        .withOut(ApproveInvoice(false))
        .getAndComplete(), // do not approve
      invoiceReceiptProcess
        .switchToCalledProcess(), // switch to Review Process (Call Activity)
      assignReviewerUT.getAndComplete(),
      reviewInvoiceUT.getAndComplete(),
      reviewInvoiceProcess.check(), // check if sub process successful
      invoiceReceiptProcess.switchToMainProcess(),
      approveInvoiceUT.getAndComplete(), // now approve
      prepareBankTransferUT.getAndComplete(),
      invoiceReceiptProcess
        .withOut(InvoiceReceiptCheck(clarified = Some(true)))
        .check()
    ),
    processScenario("Invoice Receipt with Review failed")(
      invoiceReceiptProcess.start(),
      approveInvoiceUT
        .withOut(ApproveInvoice(false))
        .getAndComplete(), // do not approve
      invoiceReceiptProcess
        .switchToCalledProcess(), // switch to Review Process (Call Activity)
      assignReviewerUT.getAndComplete(),
      reviewInvoiceUT.withOut(InvoiceReviewed(false)).getAndComplete(),
      reviewInvoiceProcess
        .withOut(InvoiceReviewed(false))
        .check(), // check if sub process successful
      invoiceReceiptProcess.switchToMainProcess(),
      invoiceReceiptProcess
        .withOut(InvoiceReceiptCheck(approved = false, clarified = Some(false)))
        .check()
    )
  )
}
