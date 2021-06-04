package camundala.examples.twitter.bpmn

import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import camundala.dsl.DSL.Givens._
import org.junit.{Rule, Test}
import org.junit.After
import org.junit.Before
import org.camunda.bpm.engine.runtime.ProcessInstance
import camundala.examples.twitter.bpmn.twitterExample._
import camundala.examples.twitter.services.{
  RejectionNotificationDelegate,
  TweetContentOfflineDelegate
}
import camundala.model._
import org.camunda.bpm.engine.test.mock.Mocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.Mocks

class TwitterProcessTest:

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
  @Deployment(
    resources = Array(
      "twitter-process.bpmn",
      "static/" + createTweetFormPath,
      "static/" + reviewTweetFormPath
    )
  )
  def testApprovedPath(): Unit =
    val tweet = TweetInputs()
    runTest(tweet, publishOnTwitterIdent, sendRejectionNotificationIdent)

  @Test
  @Deployment(
    resources = Array(
      "twitter-process.bpmn",
      "static/" + createTweetFormPath,
      "static/" + reviewTweetFormPath
    )
  )
  def testRejectedPath(): Unit =
    val tweet = TweetInputs(approved = false)
    runTest(tweet, sendRejectionNotificationIdent, publishOnTwitterIdent)

  private def runTest(
      tweet: TweetInputs,
      serviceHasPassedIdent: String,
      serviceHasNotPassedIdent: String
  ) =
    val processInstance = startProcess(tweet)
    assertThat(processInstance)
      .isStarted()
      .task()
      .hasDefinitionKey(reviewTweetUserTaskIdent)
      .hasFormKey(EmbeddedStaticForm(reviewTweetFormPath).formPathStr)
    val task = getTask(reviewTweetUserTaskIdent)
    complete(task, withVariables(tweet.approvedKey, tweet.approved))
    assertThat(processInstance)
      .isEnded()
      .hasPassed(serviceHasPassedIdent)
      .hasNotPassed(serviceHasNotPassedIdent)

  private def startProcess(tweet: TweetInputs) =
    runtimeService.startProcessInstanceByKey(
      twitterExample.twitterProcesss.ident.toString,
      withVariables(
        tweet.emailKey,
        tweet.email,
        tweet.contentKey,
        tweet.content
      )
    )

  private def getTask(id: String | Ident) =
    val taskId = id.toString
    val task = taskService.createTaskQuery.active().singleResult
    println(s"TASK: $task")
    task
