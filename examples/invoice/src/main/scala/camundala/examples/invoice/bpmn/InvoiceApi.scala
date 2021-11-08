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

object InvoiceApi extends EndpointDSL:
  val processId = "InvoiceReceipt"
  private val processName = "Invoice Process Example"

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

  lazy val processApi: ProcessApi =
    ProcessApi(processName)
      .startProcessInstance(
        processDefinitionKey = processId,
        name = processId,
        descr = "This starts the Invoice Receipt Process.",
        inExamples = InvoiceReceipt()
      )
      .evaluateDecision(
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
