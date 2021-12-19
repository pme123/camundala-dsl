package camundala.examples.invoice.bpmn

import camundala.dsl.DSL.Givens.*
import camundala.examples.invoice.bpmn.ExampleInvoice2
import camundala.examples.invoice.bpmn.ExampleInvoice2.*
import camundala.examples.invoice.bpmn.InvoiceApi.*
import camundala.examples.invoice.dsl.ProjectDSL
import camundala.model.*
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import camundala.test.*
import camundala.utest.{TestConfig, TestDsl, TestRunner}
import org.mockito.Mockito.mock

class ExampleInvoiceTest extends TestRunner, ProjectDSL:

  lazy val config: TestConfig =
    testConfig
      .deployments(
        baseResource / invoice$$v2._bpmn.path,
        baseResource / reviewInvoice._bpmn.path,
        baseResource / invoice$$v2.invoiceBDsDmn.path,
        formResource / "approve-invoice.html",
        formResource / "assign-reviewer.html",
        formResource / "prepare-bank-transfer.html",
        formResource / "review-invoice.html",
        formResource / "start-form.html"
      )
      .registries()

  @Test
  def testReviewReview(): Unit =
    test(reviewInvoiceProcess)(
      assignReviewerUT,
      reviewInvoiceUT
    )

  @Test
  def testInvoiceReceipt(): Unit =
    test(invoiceReceiptProcess)(
      approveInvoiceUT,
      prepareBankTransferUT
    )