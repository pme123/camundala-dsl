package camundala
package examples.invoice.bpmn

import camundala.api.*
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import os.*
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*

object InvoiceApiCreator extends APICreator {

  def title = "Invoice Example Process API"

  def version = "1.0"

  override lazy val serverPort = 8034

  override def description: Option[String] = super.description.map(
    _ +
      """
        |The Invoice example is provided in all of the pre-packaged distros that Camunda provides.
        |This Camunda example provides the Invoice application inside a Spring Boot application together with all
        |the necessary adjustments needed to run it out of the box. This includes:
        |
        |* The Camunda EE Webapps
        |* The Camunda Rest API
        |""".stripMargin
  )
  override def basePath: Path = pwd / "examples" / "invoice"

  import InvoiceApi.*

  def apiEndpoints: Seq[ApiEndpoint[_, _, _]] =
      invoiceReceiptProcess.endpoints ++
        approveInvoiceUT.endpoints(invoiceReceiptProcess, completeExamples = Map(
          "Invoice approved" -> ApproveInvoice(),
          "Invoice NOT approved" -> ApproveInvoice(false)
        )) ++
        prepareBankTransferUT.endpoints(invoiceReceiptProcess)

  private def invoiceReceiptEndpoints: (ApiEndpoint[_, _, _], Seq[ApiEndpoint[_, _, _]]) =
    (invoiceReceiptProcess, approveInvoiceUT)

}
