package camundala
package examples.invoice.bpmn

import api.*
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

object InvoiceApi extends pure.PureDsl:

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
  )

  enum InvoiceCategory
      derives JsonTaggedAdt.PureEncoder,
        JsonTaggedAdt.PureDecoder:
    case `Travel Expenses`
    case Misc
    case `Software License Costs`

  case class SelectApproverGroup(
      amount: Double = 30.0,
      @description(
        enumDescr(InvoiceCategory, Some("There are three possible Categories"))
      )
      invoiceCategory: InvoiceCategory =
        InvoiceCategory.`Software License Costs`
  )

  case class AssignApproverGroup(
      approverGroups: ApproverGroup = ApproverGroup.sales
  )

  enum ApproverGroup
      derives JsonTaggedAdt.PureEncoder,
        JsonTaggedAdt.PureDecoder:
    case accounting
    case sales
    case management

  @description("""Every Invoice has to be accepted by the Boss.""")
  case class ApproveInvoice(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true
  )

  @description(
    """Prepares the bank transfer for the invoice. Only readOnly fields from the Process."""
  )
  case class PrepareBankTransfer(
  )

  case class AssignedReviewer(reviewer: String = "John")
  case class InvoiceReviewed(clarified: Boolean = true)

  lazy val approveInvoiceUT: pure.UserTask[InvoiceReceipt,ApproveInvoice] = userTask(
    id = "ApproveInvoice",
    descr = "Approve the invoice (or not).",
    in = InvoiceReceipt(),
    out = ApproveInvoice()
  )

  lazy val prepareBankTransferUT = userTask(
    id = "PrepareBankTransfer",
    descr = "Prepare the bank transfer in the Financial Accounting System.",
    in = InvoiceReceipt(),
    out = PrepareBankTransfer()
  )

  lazy val invoiceReceiptProcess: pure.Process[InvoiceReceipt,NoOutput] =
    val processId = "InvoiceReceipt"
    process(
      id = processId,
      descr = "This starts the Invoice Receipt Process.",
      in = InvoiceReceipt(),
    )
  /*    .dmn(
        decisionDefinitionKey = "invoice-assign-approver",
        name = "Assign Approver Group",
        inExamples = SelectApproverGroup(),
        outExamples = AssignApproverGroup()
      )
      .userTask(
        name = "Approve Invoice",
        descr = "Approve the invoice (or not).",
        formExamples = InvoiceReceipt(),
        completeExamples = Map(
          "Invoice approved" -> ApproveInvoice(),
          "Invoice NOT approved" -> ApproveInvoice(false)
        )
      )
      .userTask(
        name = "Prepare Bank Transfer",
        descr = "Prepare the bank transfer in the Financial Accounting System.",
        formExamples = InvoiceReceipt(),
        completeExamples = PrepareBankTransfer()
      )
*/
  lazy val reviewInvoiceApi: pure.Process[InvoiceReceipt,InvoiceReviewed] =
    val processId = "ReviewInvoice"
    process(
        id = processId,
        descr = "This starts the Review Invoice Process.",
        in= InvoiceReceipt(),
        out = InvoiceReviewed()
      )
    /*  .userTask(
        name = "Assign Reviewer",
        descr = "Select the Reviewer.",
        formExamples = InvoiceReceipt(),
        completeExamples = AssignedReviewer()
      )
      .userTask(
        name = "Review Invoice",
        descr = "Review Invoice and approve.",
        formExamples = InvoiceReceipt(),
        completeExamples = Map(
          "Invoice clarified" -> InvoiceReviewed(),
          "Invoice NOT clarified" -> InvoiceReviewed(false)
        )
      )*/
