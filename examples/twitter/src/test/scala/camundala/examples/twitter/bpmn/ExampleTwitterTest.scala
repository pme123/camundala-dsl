package camundala.examples.twitter.bpmn

import camundala.dsl.DSL.Givens._
import camundala.examples.twitter.bpmn.ExampleTwitter.bpmns.processes._
import camundala.examples.twitter.bpmn.ExampleTwitter.{StartInputs, TweetAproveInputs}
import camundala.examples.twitter.services.{RejectionNotificationDelegate, TweetContentOfflineDelegate}
import camundala.model._
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import camundala.examples.twitter.bpmn.ExampleTwitter.bpmns.example__twitter
import camundala.test.TestHelper

class ExampleTwitterTest
  extends TestHelper:

  @Rule
  def processEngineRule = new ProcessEngineRule

  @Mock private var tweetContentDelegate: TweetContentOfflineDelegate = _
  @Mock private var rejectionNotificationDelegate: RejectionNotificationDelegate = _

  @Before
  def setUp(): Unit = {
    MockitoAnnotations.initMocks(this)
    Mocks.register("tweetAdapter", tweetContentDelegate)
    Mocks.register("emailAdapter", rejectionNotificationDelegate)
  }
  @Before
  def deployment(): Unit =
    val deployment = repositoryService().createDeployment()
    val resources = ExampleTwitter.config.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r => deployment.addInputStream(r, getClass().getClassLoader().getResourceAsStream(r)))
    deployment.deploy()

  @After def tearDown(): Unit = {
    Mocks.reset()
  }

  @Test
  def testApprovedPath(): Unit =
    runTest(
      StartInputs(),
      TweetAproveInputs(),
      serviceTasks.PublishOnTwitterIdent,
      serviceTasks.SendRejectionNotificationIdent
    )

  @Test
  /*@Deployment(
    resources = Array(
      "example-twitter.bpmn",
      "static/" + ExampleTwitter.createTweetFormPath,
      "static/" + ExampleTwitter.reviewTweetFormPath
    )
  )*/
  def testRejectedPath(): Unit =
    runTest(
      StartInputs(),
      TweetAproveInputs(approved = false),
      serviceTasks.SendRejectionNotificationIdent,
      serviceTasks.PublishOnTwitterIdent
    )

  private def runTest(
      startInputs: StartInputs,
      tweetAproveInputs: TweetAproveInputs,
      serviceHasPassedIdent: String,
      serviceHasNotPassedIdent: String
  ) =
    val processInstance = startProcess(startInputs)
    assertThat(processInstance)
      .isStarted()
      .task()
      .hasDefinitionKey(userTasks.ReviewTweetIdent)
      .hasFormKey(
        EmbeddedStaticForm(ExampleTwitter.reviewTweetFormPath).formPathStr
      )
    val task = getTask(userTasks.ReviewTweetIdent)
    complete(task, tweetAproveInputs.asVariables)
    assertThat(processInstance)
      .isEnded()
      .hasPassed(serviceHasPassedIdent)
      .hasNotPassed(serviceHasNotPassedIdent)

  private def startProcess(tweet: StartInputs) =
    runtimeService.startProcessInstanceByKey(
      TwitterDemoProcess.ident.toString,
      tweet.asVariables
    )

  private def getTask(id: String | Ident) =
    val taskId = id.toString
    val task = taskService.createTaskQuery.active().singleResult
    println(s"TASK: $task")
    task
