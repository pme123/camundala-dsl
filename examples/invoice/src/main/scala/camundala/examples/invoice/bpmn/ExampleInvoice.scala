package camundala
package examples.invoice
package bpmn
import camundala.dev.*

import camundala.dsl.DSL

import java.io.File

import camundala.model.BpmnsConfig

object ExampleInvoice2 extends DSL:

  final val cawemoFolder = "./examples/invoice/cawemo"

  final val withIdFolder = "./examples/invoice/cawemo/with-ids"

  final val generatedFolder = "./examples/invoice/src/main/resources"

  val config = bpmnsConfig
    .users(
    )
    .groups(
    )
    .bpmns(
      invoice_v2._bpmn,
      reviewInvoice._bpmn
    )

  object users:

    println("//TODO Add users here or remove this object")
  end users

  object groups:

    println("//TODO Add groups here or remove this object")
  end groups

  object invoice_v2:

    val _bpmn = bpmn("invoice.v2")
      .processes(
        processes.InvoiceReceipt
      )

    object processes:

      val InvoiceReceipt = process("InvoiceReceipt")
        .starterGroups(
        )
        .starterUsers(
        )
        .nodes(
          startEvents.Invoicereceived,
          userTasks.ApproveInvoice,
          userTasks.PrepareBankTransfer,
          serviceTasks.ArchiveInvoice,
          businessRuleTasks.AssignApproverGroup,
          exclusiveGateways.Invoiceapproved,
          exclusiveGateways.Reviewsuccessful,
          endEvents.InvoiceNotprocessed,
          endEvents.Invoiceprocessed
        )
        .flows(
          flows.SequenceFlow_3__ArchiveInvoice__Invoiceprocessed,
          flows.SequenceFlow_1__Invoicereceived__AssignApproverGroup,
          flows.SequenceFlow_2__PrepareBankTransfer__ArchiveInvoice,
          flows.sequenceFlow_178__AssignApproverGroup__ApproveInvoice,
          flows.Yes__Reviewsuccessful__ApproveInvoice,
          flows.No__Reviewsuccessful__InvoiceNotprocessed,
          flows.Yes__Invoiceapproved__PrepareBankTransfer,
          flows.sequenceFlow_183__ReviewInvoice__Reviewsuccessful,
          flows.sequenceFlow_180__ApproveInvoice__Invoiceapproved,
          flows.No__Invoiceapproved__ReviewInvoice
        )
      object userTasks:

        val ApproveInvoiceIdent = "ApproveInvoice"

        lazy val ApproveInvoice = userTask(ApproveInvoiceIdent)

        val PrepareBankTransferIdent = "PrepareBankTransfer"

        lazy val PrepareBankTransfer = userTask(PrepareBankTransferIdent)
      end userTasks

      object businessRuleTasks:

        val AssignApproverGroupIdent = "AssignApproverGroup"

        lazy val AssignApproverGroup = businessRuleTask(
          AssignApproverGroupIdent
        )
      end businessRuleTasks

      object endEvents:

        val InvoiceNotprocessedIdent = "InvoiceNotprocessed"

        lazy val InvoiceNotprocessed = endEvent(InvoiceNotprocessedIdent)

        val InvoiceprocessedIdent = "Invoiceprocessed"

        lazy val Invoiceprocessed = endEvent(InvoiceprocessedIdent)
      end endEvents

      object serviceTasks:

        val ArchiveInvoiceIdent = "ArchiveInvoice"

        lazy val ArchiveInvoice = serviceTask(ArchiveInvoiceIdent)
      end serviceTasks

      object startEvents:

        val InvoicereceivedIdent = "Invoicereceived"

        lazy val Invoicereceived = startEvent(InvoicereceivedIdent)
      end startEvents

      object exclusiveGateways:

        val InvoiceapprovedIdent = "Invoiceapproved"

        lazy val Invoiceapproved = exclusiveGateway(InvoiceapprovedIdent)

        val ReviewsuccessfulIdent = "Reviewsuccessful"

        lazy val Reviewsuccessful = exclusiveGateway(ReviewsuccessfulIdent)
      end exclusiveGateways

      object flows:

        val SequenceFlow_3__ArchiveInvoice__InvoiceprocessedIdent =
          "SequenceFlow_3__ArchiveInvoice__Invoiceprocessed"

        lazy val SequenceFlow_3__ArchiveInvoice__Invoiceprocessed =
          sequenceFlow(SequenceFlow_3__ArchiveInvoice__InvoiceprocessedIdent)

        val SequenceFlow_1__Invoicereceived__AssignApproverGroupIdent =
          "SequenceFlow_1__Invoicereceived__AssignApproverGroup"

        lazy val SequenceFlow_1__Invoicereceived__AssignApproverGroup =
          sequenceFlow(
            SequenceFlow_1__Invoicereceived__AssignApproverGroupIdent
          )

        val SequenceFlow_2__PrepareBankTransfer__ArchiveInvoiceIdent =
          "SequenceFlow_2__PrepareBankTransfer__ArchiveInvoice"

        lazy val SequenceFlow_2__PrepareBankTransfer__ArchiveInvoice =
          sequenceFlow(SequenceFlow_2__PrepareBankTransfer__ArchiveInvoiceIdent)

        val sequenceFlow_178__AssignApproverGroup__ApproveInvoiceIdent =
          "sequenceFlow_178__AssignApproverGroup__ApproveInvoice"

        lazy val sequenceFlow_178__AssignApproverGroup__ApproveInvoice =
          sequenceFlow(
            sequenceFlow_178__AssignApproverGroup__ApproveInvoiceIdent
          )

        val Yes__Reviewsuccessful__ApproveInvoiceIdent =
          "Yes__Reviewsuccessful__ApproveInvoice"

        lazy val Yes__Reviewsuccessful__ApproveInvoice = sequenceFlow(
          Yes__Reviewsuccessful__ApproveInvoiceIdent
        )

        val No__Reviewsuccessful__InvoiceNotprocessedIdent =
          "No__Reviewsuccessful__InvoiceNotprocessed"

        lazy val No__Reviewsuccessful__InvoiceNotprocessed = sequenceFlow(
          No__Reviewsuccessful__InvoiceNotprocessedIdent
        )

        val Yes__Invoiceapproved__PrepareBankTransferIdent =
          "Yes__Invoiceapproved__PrepareBankTransfer"

        lazy val Yes__Invoiceapproved__PrepareBankTransfer = sequenceFlow(
          Yes__Invoiceapproved__PrepareBankTransferIdent
        )

        val sequenceFlow_183__ReviewInvoice__ReviewsuccessfulIdent =
          "sequenceFlow_183__ReviewInvoice__Reviewsuccessful"

        lazy val sequenceFlow_183__ReviewInvoice__Reviewsuccessful =
          sequenceFlow(sequenceFlow_183__ReviewInvoice__ReviewsuccessfulIdent)

        val sequenceFlow_180__ApproveInvoice__InvoiceapprovedIdent =
          "sequenceFlow_180__ApproveInvoice__Invoiceapproved"

        lazy val sequenceFlow_180__ApproveInvoice__Invoiceapproved =
          sequenceFlow(sequenceFlow_180__ApproveInvoice__InvoiceapprovedIdent)

        val No__Invoiceapproved__ReviewInvoiceIdent =
          "No__Invoiceapproved__ReviewInvoice"

        lazy val No__Invoiceapproved__ReviewInvoice = sequenceFlow(
          No__Invoiceapproved__ReviewInvoiceIdent
        )
      end flows
    end processes

  object reviewInvoice:

    val _bpmn =
      bpmn("reviewInvoice")
        .processes(
          processes.ReviewInvoiceProcess
        )

    object processes:

      val ReviewInvoiceProcess = process("ReviewInvoiceProcess")
        .starterGroups(
        )
        .starterUsers(
        )
        .nodes(
          startEvents.StartEvent_1,
          userTasks.AssignReviewer,
          userTasks.ReviewInvoice,
          endEvents.EndEvent_1og1zom
        )
        .flows(
          flows.SequenceFlow_1ggutts__StartEvent_1__AssignReviewer,
          flows.SequenceFlow_144f11w__AssignReviewer__ReviewInvoice,
          flows.SequenceFlow_0vvoxt0__ReviewInvoice__EndEvent_1og1zom
        )
      object userTasks:

        val AssignReviewerIdent = "AssignReviewer"

        lazy val AssignReviewer =
          userTask(AssignReviewerIdent)
            .staticForm("forms/assign-reviewer.html")

        val ReviewInvoiceIdent = "ReviewInvoice"

        lazy val ReviewInvoice =
          userTask(ReviewInvoiceIdent)
            .staticForm("forms/review-invoice.html")
      end userTasks

      object endEvents:

        val EndEvent_1og1zomIdent = "EndEvent_1og1zom"

        lazy val EndEvent_1og1zom = endEvent(EndEvent_1og1zomIdent)
      end endEvents

      object startEvents:

        val StartEvent_1Ident = "StartEvent_1"

        lazy val StartEvent_1 = startEvent(StartEvent_1Ident)
      end startEvents

      object flows:

        val SequenceFlow_1ggutts__StartEvent_1__AssignReviewerIdent =
          "SequenceFlow_1ggutts__StartEvent_1__AssignReviewer"

        lazy val SequenceFlow_1ggutts__StartEvent_1__AssignReviewer =
          sequenceFlow(SequenceFlow_1ggutts__StartEvent_1__AssignReviewerIdent)

        val SequenceFlow_144f11w__AssignReviewer__ReviewInvoiceIdent =
          "SequenceFlow_144f11w__AssignReviewer__ReviewInvoice"

        lazy val SequenceFlow_144f11w__AssignReviewer__ReviewInvoice =
          sequenceFlow(SequenceFlow_144f11w__AssignReviewer__ReviewInvoiceIdent)

        val SequenceFlow_0vvoxt0__ReviewInvoice__EndEvent_1og1zomIdent =
          "SequenceFlow_0vvoxt0__ReviewInvoice__EndEvent_1og1zom"

        lazy val SequenceFlow_0vvoxt0__ReviewInvoice__EndEvent_1og1zom =
          sequenceFlow(
            SequenceFlow_0vvoxt0__ReviewInvoice__EndEvent_1og1zomIdent
          )
      end flows
    end processes
  end reviewInvoice
end ExampleInvoice2
