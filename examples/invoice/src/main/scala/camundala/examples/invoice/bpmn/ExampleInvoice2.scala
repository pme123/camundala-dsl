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
    .dmns(
      invoice$$v2.invoiceBDsDmn
    )
    .users(
      users.demo
    )
    .groups(
      groups.accounting
    )
    .bpmns(
      invoice$$v2._bpmn,
      reviewInvoice._bpmn
    )

  object users:

    val demo = user("demo")

  end users

  object groups:

    val accounting = group("accounting")
  end groups

  object invoice$$v2:
    val invoiceBDsDmn = dmn("invoiceBusinessDecisions")
    val _bpmn = bpmn("invoice$$v2")
      .processes(
        processes.InvoiceReceipt
      )

    object processes:

      val InvoiceReceipt = process("InvoiceReceiptP")
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
          flows.Yes__Reviewsuccessful__ApproveInvoice,
          flows.No__Reviewsuccessful__InvoiceNotprocessed,
          flows.Yes__Invoiceapproved__PrepareBankTransfer,
          flows.No__Invoiceapproved__ReviewInvoice
        )

      object userTasks:

        val ApproveInvoiceIdent = "ApproveInvoiceUT"

        lazy val ApproveInvoice = userTask(ApproveInvoiceIdent)
          .candidateGroup("${approverGroups}")
          .staticForm("forms/approve-invoice.html")
          .dueDate("${dateTime().plusWeeks(1).toDate()}")
          .taskListeners(
            taskListener.create
              .inlineJavascript("""if(!!task.getVariable('approver')) {
                                  |  task.setAssignee(approver);
                                  |}""".stripMargin),
            taskListener.assignment
              .inlineJavascript(
                "task.setVariable('approver', task.getAssignee());"
              )
          )

        val PrepareBankTransferIdent = "PrepareBankTransferUT"

        lazy val PrepareBankTransfer = userTask(PrepareBankTransferIdent)
          .staticForm("forms/prepare-bank-transfer.html")
      end userTasks

      object callActivities:

        val ReviewInvoiceIdent = "ReviewInvoiceCA"

        lazy val ReviewInvoice =
          callActivity(ReviewInvoiceIdent)
            .calledElement(reviewInvoice.processes.ReviewInvoiceProcessIdent)
            .processBusinessKey
            .inSource("invoiceDocument")
            .inSource("creditor")
            .inSource("amount")
            .inSource("invoiceCategory")
            .inSource("invoiceNumber")
            .outSource("clarified")

      end callActivities

      object businessRuleTasks:

        val AssignApproverGroupIdent = "AssignApproverGroupBRT"

        lazy val AssignApproverGroup = businessRuleTask(
          AssignApproverGroupIdent
        ).impl(
          dmnTable("invoice-assign-approver").latest
            .collectEntries("approverGroups")
        )
      end businessRuleTasks

      object endEvents:

        val InvoiceNotprocessedIdent = "InvoiceNotProcessedEE"

        lazy val InvoiceNotprocessed = endEvent(InvoiceNotprocessedIdent)

        val InvoiceprocessedIdent = "InvoiceProcessedEE"

        lazy val Invoiceprocessed = endEvent(InvoiceprocessedIdent)
      end endEvents

      object serviceTasks:

        val ArchiveInvoiceIdent = "ArchiveInvoiceST"

        lazy val ArchiveInvoice = serviceTask(ArchiveInvoiceIdent)
          .javaClass(ArchiveInvoiceService.className)
          .asyncBefore

      end serviceTasks

      object startEvents:

        val InvoicereceivedIdent = "InvoiceReceivedSE"

        lazy val Invoicereceived =
          startEvent(InvoicereceivedIdent)
            .staticForm("forms/start-form.html")
      end startEvents

      object exclusiveGateways:

        val InvoiceapprovedIdent = "InvoiceApprovedEG"

        lazy val Invoiceapproved = exclusiveGateway(InvoiceapprovedIdent)

        val ReviewsuccessfulIdent = "ReviewSuccessfulEG"

        lazy val Reviewsuccessful = exclusiveGateway(ReviewsuccessfulIdent)
      end exclusiveGateways

      object flows:

        val Yes__Reviewsuccessful__ApproveInvoiceIdent =
          "YesSF__ReviewSuccessfulEG__ApproveInvoiceUT"

        lazy val Yes__Reviewsuccessful__ApproveInvoice = sequenceFlow(
          Yes__Reviewsuccessful__ApproveInvoiceIdent
        ).expression("clarified")

        val No__Reviewsuccessful__InvoiceNotprocessedIdent =
          "NoSF__ReviewSuccessfulEG__InvoiceNotProcessedEE"

        lazy val No__Reviewsuccessful__InvoiceNotprocessed = sequenceFlow(
          No__Reviewsuccessful__InvoiceNotprocessedIdent
        ).expression("!clarified")

        val Yes__Invoiceapproved__PrepareBankTransferIdent =
          "YesSF__InvoiceApprovedEG__PrepareBankTransferUT"

        lazy val Yes__Invoiceapproved__PrepareBankTransfer = sequenceFlow(
          Yes__Invoiceapproved__PrepareBankTransferIdent
        ).expression("approved")

        val No__Invoiceapproved__ReviewInvoiceIdent =
          "NoSF__InvoiceApprovedEG__ReviewInvoiceCA"

        lazy val No__Invoiceapproved__ReviewInvoice = sequenceFlow(
          No__Invoiceapproved__ReviewInvoiceIdent
        ).expression("!approved")
    end processes
  end invoice$$v2

  object reviewInvoice:

    val _bpmn = bpmn("reviewInvoice")
      .processes(
        processes.ReviewInvoiceProcess
      )

    object processes:
      val ReviewInvoiceProcessIdent = "ReviewInvoiceP"
      val ReviewInvoiceProcess = process(ReviewInvoiceProcessIdent)
    /*    .tasks(
          userTasks.AssignReviewer,
          userTasks.ReviewInvoice
        ) */
      object userTasks:

        val AssignReviewerIdent = "AssignReviewerUT"

        lazy val AssignReviewer =
          userTask(AssignReviewerIdent)
            .assignee(users.demo.ref)
            .staticForm("forms/assign-reviewer.html")

        val ReviewInvoiceIdent = "ReviewInvoiceUT"

        lazy val ReviewInvoice =
          userTask(ReviewInvoiceIdent)
            .assignee("${reviewer}")
            .staticForm("forms/review-invoice.html")
      end userTasks

    end processes
  end reviewInvoice

end ExampleInvoice2
