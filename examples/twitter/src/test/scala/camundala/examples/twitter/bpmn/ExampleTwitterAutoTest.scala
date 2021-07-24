package camundala.examples.twitter.bpmn

import camundala.dsl.DSL.Givens._
import camundala.examples.twitter.bpmn.ExampleTwitter.bpmns.processes._
import camundala.examples.twitter.bpmn.ExampleTwitter.{
  StartInputs,
  TweetAproveInputs
}
import camundala.examples.twitter.services.{
  RejectionNotificationDelegate,
  TweetContentOfflineDelegate
}
import camundala.model._
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import camundala.test.TestHelper
import camundala.examples.twitter.bpmn.ExampleTwitter.bpmns

class ExampleTwitterAutoTest extends TestHelper:

  val bpmnsConfigToTest = ExampleTwitter.config

  @Rule
  def processEngineRule = new ProcessEngineRule

  @Mock private var tweetContentDelegate: TweetContentOfflineDelegate = _
  @Mock private var rejectionNotificationDelegate
      : RejectionNotificationDelegate = _

  @Before
  def setUp(): Unit = {
    MockitoAnnotations.initMocks(this)
    Mocks.register("tweetAdapter", tweetContentDelegate)
    Mocks.register("emailAdapter", rejectionNotificationDelegate)
  }

  @After def tearDown(): Unit = {
    Mocks.reset()
  }

  @Test
  def test(): Unit =
    println("test-run")
