package camundala
package examples.invoice.bpmn

import camundala.api.*
import camundala.api.endpoints.*
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import os.*
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*

object InvoiceApiCreator extends APICreator {

  def title = "Invoice Example Process API"

  def version = "1.0"

  override lazy val serverPort = 8034
  
  override def basePath: Path = pwd / "examples" / "invoice"

  import InvoiceApi.*

  def apiEndpoints: Seq[ApiEndpoints] =
    Seq(
      invoiceReceiptProcess
        .endpoints(
          invoiceAssignApproverDMN.endpoint,
          approveInvoiceUT.endpoint
            .withOutExample("Invoice approved", ApproveInvoice())
            .withOutExample("Invoice NOT approved", ApproveInvoice(false)),
          prepareBankTransferUT.endpoint
        ),
      reviewInvoiceProcess.endpoints(
        assignReviewerUT.endpoint,
        reviewInvoiceUT.endpoint
          .withOutExample("Invoice clarified", InvoiceReviewed())
          .withOutExample("Invoice NOT clarified", InvoiceReviewed(false))
      )
    )

}
