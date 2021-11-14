package camundala
package examples.invoice.bpmn

import camundala.api.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// One import for this ADT/JSON codec
import org.latestbit.circe.adt.codec.*

object InvoiceApi extends PureDsl:

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
      @description(
        enumDescr(
          ApproverGroup,
          Some("The following Groups can approve the invoice:")
        )
      )
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

  val invoiceReceiptProcess =
    val processId = "InvoiceReceipt"
    process(
      id = processId,
      descr = "This starts the Invoice Receipt Process.",
      in = InvoiceReceipt()
    )

  val invoiceAssignApproverDMN = dmn(
    decisionDefinitionKey = "invoice-assign-approver",
    hitPolicy = HitPolicy.COLLECT,
    id = "Assign Approver Group",
    in = SelectApproverGroup(),
    out = AssignApproverGroup()
  )

  val approveInvoiceUT =
    userTask(
      id = "ApproveInvoice",
      descr = "Approve the invoice (or not).",
      in = InvoiceReceipt(),
      out = ApproveInvoice()
    )

  val prepareBankTransferUT = userTask(
    id = "PrepareBankTransfer",
    descr = "Prepare the bank transfer in the Financial Accounting System.",
    in = InvoiceReceipt(),
    out = PrepareBankTransfer()
  )

  val reviewInvoiceProcess: Process[InvoiceReceipt, InvoiceReviewed] =
    val processId = "ReviewInvoice"
    process(
      id = processId,
      descr = "This starts the Review Invoice Process.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
  val assignReviewerUT = userTask(
    id = "AssignReviewer",
    descr = "Select the Reviewer.",
    in = InvoiceReceipt(),
    out = AssignedReviewer()
  )
  val reviewInvoiceUT = userTask(
    id = "ReviewInvoice",
    descr = "Review Invoice and approve.",
    in = InvoiceReceipt(),
    out = InvoiceReviewed()
  )
