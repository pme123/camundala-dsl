package camundala
package examples.invoice
package bpmn

import camundala.dev
import org.camunda.bpm.example.invoice.service.ArchiveInvoiceService

object ExampleInvoice2 extends DSL:

  final val cawemoFolder = "./examples/invoice/cawemo"

  final val withIdFolder = "./examples/invoice/cawemo/with-ids"

  final val generatedFolder = "./examples/invoice/src/main/resources"

  val config = bpmnsConfig
    .users(
    )
    .groups(
      groups.accounting
    )
    .bpmns(
      invoice$$v2._bpmn,
      reviewInvoice._bpmn
    )

  object users:

    println("//TODO Add users here or remove this object")
  end users

  object groups:

    val accounting = group("accounting")
  end groups

  object invoice$$v2:

    val _bpmn = bpmn("invoice$$v2")
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
          callActivities.ReviewInvoice,
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
          .candidateGroup("${approverGroups}")
          .staticForm("forms/approve-invoice.html")
          .dueDate("${dateTime().plusWeeks(1).toDate()}")
          .listeners(
            taskListener.create
              .inlineJavascript("""if(!!task.getVariable('approver')) {
                                  |  task.setAssignee(approver);
                                  |}""".stripMargin),
            taskListener.assignment
              .inlineJavascript(
                "task.setVariable('approver', task.getAssignee());"
              )
          )

        val PrepareBankTransferIdent = "PrepareBankTransfer"

        lazy val PrepareBankTransfer = userTask(PrepareBankTransferIdent)
          .staticForm("forms/prepare-bank-transfer.html")
      end userTasks

      object callActivities:

        val ReviewInvoiceIdent = "ReviewInvoice"

        lazy val ReviewInvoice =
          callActivity(ReviewInvoiceIdent).processBusinessKey
            .inSource("invoiceDocument")
            .inSource("creditor")
            .inSource("amount")
            .inSource("invoiceCategory")
            .inSource("invoiceNumber")
            .outSource("clarified")

      end callActivities

      object businessRuleTasks:

        val AssignApproverGroupIdent = "AssignApproverGroup"

        lazy val AssignApproverGroup = businessRuleTask(
          AssignApproverGroupIdent
        ).impl(
          dmn("invoice-assign-approver").latest
            .collectEntries("approverGroups")
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
          .javaClass(ArchiveInvoiceService.className)
          .asyncBefore

      end serviceTasks

      object startEvents:

        val InvoicereceivedIdent = "Invoicereceived"

        lazy val Invoicereceived =
          startEvent(InvoicereceivedIdent)
            .staticForm("forms/start-form.html")
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
  end invoice$$v2

  object reviewInvoice:

    val _bpmn = bpmn("reviewInvoice")
      .processes(
        processes.ReviewInvoiceProcess
      )

    object processes:

      val ReviewInvoiceProcess = process("ReviewInvoiceProcess")
        .nodes(
          userTasks.AssignReviewer,
          userTasks.ReviewInvoice
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
    end processes
  end reviewInvoice

end ExampleInvoice2
