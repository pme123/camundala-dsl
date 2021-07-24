package camundala.examples.twitter.bpmn

import camundala.dsl.DSL.Givens._
import camundala.examples.invoice.bpmn.ExampleInvoice2
import camundala.model._
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import camundala.test.TestHelper

class ExampleInvoiceTest
  extends TestHelper:

  val bpmnsConfigToTest = ExampleInvoice2.config

  @Rule
  def processEngineRule = new ProcessEngineRule

  @Before
  def setUp(): Unit = {
    MockitoAnnotations.initMocks(this)
  }

  @After def tearDown(): Unit = {
    Mocks.reset()
  }

  @Test
  def testTODO(): Unit =
    println("TODO with automatic testing")

