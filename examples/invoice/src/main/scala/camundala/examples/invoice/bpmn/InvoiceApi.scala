package camundala
package examples.invoice.bpmn

import api.*
import bpmn.*
import domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// One import for this ADT/JSON codec
import org.latestbit.circe.adt.codec.*

object InvoiceApi extends PureDsl:

  val invoiceCategoryDescr: String =
    enumDescr[InvoiceCategory](Some("There are three possible Categories"))

  @description("Received Invoice that need approval.")
  case class InvoiceReceipt(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 30.0,
      @description(invoiceCategoryDescr)
      invoiceCategory: InvoiceCategory =
        InvoiceCategory.`Software License Costs`,
      invoiceNumber: String = "I-12345",
      invoiceDocument: FileInOut = FileInOut(
        "invoice.pdf",
        read.bytes(
          pwd / "examples" / "invoice" / "src" / "main" / "resources" / "invoice.pdf"
          //    pwd / "src" / "main" / "resources" / "invoice.pdf"
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

  case class AssignApproverGroup(
      @description(approverGroupDescr)
      approverGroups: ApproverGroup = ApproverGroup.management
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
  case class InvoiceReviewed(clarified: Boolean = true)

  lazy val invoiceReceiptProcess =
    val processId = "InvoiceReceipt"
    process(
      id = processId,
      descr = "This starts the Invoice Receipt Process.",
      in = InvoiceReceipt()
    )

  lazy val invoiceAssignApproverDMN
      : DecisionDmn[SelectApproverGroup, ManyInOut[AssignApproverGroup]] = dmn(
    decisionDefinitionKey = "invoice-assign-approver",
    hitPolicy = HitPolicy.COLLECT,
    id = "AssignApproverGroup",
    in = SelectApproverGroup(),
    out = ManyInOut(AssignApproverGroup())
  )

  lazy val invoiceAssignApproverDMN2
      : DecisionDmn[SelectApproverGroup, ManyInOut[AssignApproverGroup]] =
    invoiceAssignApproverDMN
      .withIn(SelectApproverGroup(1050, InvoiceCategory.`Travel Expenses`))
      .withOut(
        ManyInOut(
          AssignApproverGroup(ApproverGroup.accounting),
          AssignApproverGroup(ApproverGroup.sales)
        )
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
