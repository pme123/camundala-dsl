package camundala
package examples.invoice.bpmn

import camundala.api.*
import camundala.api.CamundaVariable.*
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import os.*
import sttp.tapir.Schema.annotations.description
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.{Endpoint, Schema, SchemaType}

import java.io.File
import java.util.Base64

// One import for this ADT/JSON codec
import org.latestbit.circe.adt.codec._

object InvoiceApi extends EndpointDSL:
  val processId = "InvoiceReceipt"
  private val tag = "Invoice Process Example"

  @description("Received Invoice that need approval.")
  case class InvoiceReceipt(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 30.0,
      @description(
        enumDescr(InvoiceCategory, Some("There are three possible Categories"))
      )
      invoiceCategory: InvoiceCategory =
        InvoiceCategory.`Software License Costs`,
      invoiceNumber: String = "I-12345",
      invoiceDocument: FileInOut = FileInOut(
        "invoice.pdf",
        read.bytes(
          pwd / "examples" / "invoice" / "src" / "main" / "resources" / "invoice.pdf"
        ),
        Some("application/pdf")
      )
  ) extends InOutObject

  enum InvoiceCategory
      derives JsonTaggedAdt.PureEncoder,
        JsonTaggedAdt.PureDecoder:
    case `Travel Expenses`
    case Misc
    case `Software License Costs`

  @description("""Every Invoice has to be accepted by the Boss.""")
  case class ApproveInvoice(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true
  ) extends InOutObject

  @description(
    """Prepares the bank transfer for the invoice. Only readOnly fields from the Process."""
  )
  case class PrepareBankTransfer(
  ) extends InOutObject

  lazy val apiEndpoints: Seq[ApiEndpoint[_, _, _]] =
    endpoints(

      startProcessInstance(
        processId,
        processId,
        tag,
        "This starts the Invoice Receipt Process.",
        InvoiceReceipt()
      ),

      userTask(
        "Approve Invoice",
        tag,
        "Approve the invoice (or not).",
        InvoiceReceipt(),
        Map(
          "Invoice approved" -> ApproveInvoice(),
          "Invoice NOT approved" -> ApproveInvoice(false)
        )
      ),

      userTask(
        "Prepare Bank Transfer",
        tag,
        "Prepare the bank transfer in the Financial Accounting System.",
        InvoiceReceipt(),
        PrepareBankTransfer()
      )

    )