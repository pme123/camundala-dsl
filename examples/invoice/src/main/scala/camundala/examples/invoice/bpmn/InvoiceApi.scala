package camundala
package examples.invoice.bpmn

import api.*
import bpmn.*
import domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// One import for this ADT/JSON codec
import org.latestbit.circe.adt.codec.*

object InvoiceApi extends BpmnDsl:

  val invoiceCategoryDescr: String =
    enumDescr[InvoiceCategory](Some("There are three possible Categories"))

  @description("Received Invoice that need approval.")
  case class InvoiceReceipt(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 300.0,
      @description(invoiceCategoryDescr)
      invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
      invoiceNumber: String = "I-12345",
      invoiceDocument: FileInOut = FileInOut(
        "invoice.pdf",
        read.bytes(
          os.resource / "invoice.pdf"
        ),
        Some("application/pdf")
      )
  )

  enum InvoiceCategory derives JsonTaggedAdt.PureEncoder:
    case `Travel Expenses`
    case Misc
    case `Software License Costs`

  case class SelectApproverGroup(
      amount: Double = 30.0,
      @description(invoiceCategoryDescr)
      invoiceCategory: InvoiceCategory =
        InvoiceCategory.`Software License Costs`
  )

  val approverGroupDescr: String = enumDescr[ApproverGroup](
    Some("The following Groups can approve the invoice:")
  )

  case class AssignApproverGroups(
      @description(approverGroupDescr)
      approverGroups: Seq[ApproverGroup] = Seq(ApproverGroup.management)
  )

  enum ApproverGroup derives JsonTaggedAdt.PureEncoder:
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
  case class InvoiceReviewed(
      @description("Flag that is set by the Reviewer")
      clarified: Boolean = true
  )

  case class InvoiceReceiptCheck(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true,
      @description("Flag that is set by the Reviewer (only set if there was a review).")
      clarified: Option[Boolean] = None
  )

  lazy val invoiceReceiptProcess =
    val processId = "InvoiceReceipt"
    process(
      id = processId,
      descr = "This starts the Invoice Receipt Process.",
      in = InvoiceReceipt(),
      out = InvoiceReceiptCheck() // just for testing
    )

  lazy val invoiceAssignApproverDMN
      : DecisionDmn[SelectApproverGroup, AssignApproverGroups] = collectEntries(
    decisionDefinitionKey = "invoice-assign-approver",
    in = SelectApproverGroup(),
    out = AssignApproverGroups()
  )

  lazy val invoiceAssignApproverDMN2
      : DecisionDmn[SelectApproverGroup, AssignApproverGroups] =
    invoiceAssignApproverDMN
      .withIn(SelectApproverGroup(1050, InvoiceCategory.`Travel Expenses`))
      .withOut(
        AssignApproverGroups(Seq(ApproverGroup.accounting, ApproverGroup.sales))
      )

  lazy val approveInvoiceUT =
    userTask(
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

  lazy val archiveInvoiceST = serviceTask(
    id = "ArchiveInvoice",
    descr = "Archive the Invoice."
  )

  lazy val reviewInvoiceProcess: Process[InvoiceReceipt, InvoiceReviewed] =
    val processId = "ReviewInvoiceProcess"
    process(
      id = processId,
      descr = "This starts the Review Invoice Process.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
  lazy val assignReviewerUT = userTask(
    id = "AssignReviewer",
    descr = "Select the Reviewer.",
    in = InvoiceReceipt(),
    out = AssignedReviewer()
  )
  lazy val reviewInvoiceUT = userTask(
    id = "ReviewInvoice",
    descr = "Review Invoice and approve.",
    in = InvoiceReceipt(),
    out = InvoiceReviewed()
  )

  // CAWEMO: /Users/mpa/dev/Github/pme123/camundala-dsl/examples/invoice/cawemo/invoice.v2.bpmn

  val InvoiceReceiptPIdent = "InvoiceReceiptPIdent"
  lazy val InvoiceReceiptP = process(
    InvoiceReceiptPIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val ApproveInvoiceUTIdent = "ApproveInvoiceUTIdent"
  lazy val ApproveInvoiceUT = process(
    ApproveInvoiceUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val PrepareBankTransferUTIdent = "PrepareBankTransferUTIdent"
  lazy val PrepareBankTransferUT = process(
    PrepareBankTransferUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val ArchiveInvoiceSTIdent = "ArchiveInvoiceSTIdent"
  lazy val ArchiveInvoiceST = process(
    ArchiveInvoiceSTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val AssignApproverGroupBRTIdent = "AssignApproverGroupBRTIdent"
  lazy val AssignApproverGroupBRT = process(
    AssignApproverGroupBRTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  // WITH IDS: /Users/mpa/dev/Github/pme123/camundala-dsl/examples/invoice/cawemo/with-ids/invoice.v2.bpmn
  // CAWEMO: /Users/mpa/dev/Github/pme123/camundala-dsl/examples/invoice/cawemo/reviewInvoice.bpmn

  val ReviewInvoicePIdent = "ReviewInvoicePIdent"
  lazy val ReviewInvoiceP = process(
    ReviewInvoicePIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val AssignReviewerUTIdent = "AssignReviewerUTIdent"
  lazy val AssignReviewerUT = process(
    AssignReviewerUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val ReviewInvoiceUTIdent = "ReviewInvoiceUTIdent"
  lazy val ReviewInvoiceUT = process(
    ReviewInvoiceUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

// WITH IDS: /Users/mpa/dev/Github/pme123/camundala-dsl/examples/invoice/cawemo/with-ids/reviewInvoice.bpmn
