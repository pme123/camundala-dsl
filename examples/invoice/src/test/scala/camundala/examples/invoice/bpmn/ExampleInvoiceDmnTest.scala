package camundala.examples.invoice.bpmn

import camundala.dsl.DSL.Givens.*
import camundala.examples.invoice.bpmn.ExampleInvoice2
import camundala.examples.invoice.bpmn.ExampleInvoice2.*
import camundala.examples.invoice.bpmn.InvoiceApi.*
import camundala.examples.invoice.dsl.ProjectDSL
import camundala.model.*
import camundala.test.*
import camundala.utest.{DmnTestRunner, TestConfig, TestDsl, TestRunner}
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.Mockito.mock
import org.mockito.{Mock, MockitoAnnotations}
import os.Path

class ExampleInvoiceDmnTest extends DmnTestRunner, ProjectDSL:

  val dmnPath = baseResource / invoice$$v2.invoiceBDsDmn.path

  @Test
  def testSingleResult(): Unit =
    test(invoiceAssignApproverDMN)

  @Test
  def testMoreResult(): Unit =
    test(invoiceAssignApproverDMN2)
