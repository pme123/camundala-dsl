package camundala
package examples.invoice.bpmn

import camundala.api.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object InvoiceApiCreator extends APICreator {

  def title = "Invoice Example Process API"

  def version = "1.0"

  override lazy val serverPort = 8034
  
  override def basePath: Path = pwd / "examples" / "invoice"

  import InvoiceApi.*

  apiEndpoints(
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
