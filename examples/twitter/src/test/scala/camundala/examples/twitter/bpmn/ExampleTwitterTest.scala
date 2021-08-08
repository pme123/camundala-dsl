package camundala
package examples.twitter
package bpmn

import ExampleTwitter.bpmns.processes.*
import ExampleTwitter.{StartInputs, TweetAproveInputs}
import services.{RejectionNotificationDelegate, TweetContentOfflineDelegate}
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import org.mockito.Mockito.*
import camundala.examples.twitter.bpmn.ExampleTwitter.bpmns.{example__twitter, processes}
import camundala.test.{BpmnProcessTester, ServiceRegistry, TestDSL, TestHelper}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests

class ExampleTwitterTest extends //TestHelper,
  ProjectDSL, TestDSL:

  @Mock private var tweetContentDelegate: TweetContentOfflineDelegate = _
  @Mock private var rejectionNotificationDelegate: RejectionNotificationDelegate = _
  @Rule
  def processEngineRule = new ProcessEngineRule

  def tester: BpmnProcessTester =
    tester(TwitterDemoProcess) {
      testConfig
        .deployments(
          example__twitter.path,
          formResource(createTweetFormPath),
          formResource(reviewTweetFormPath)
        )
        .registries(
          serviceRegistry(
            dsl.emailAdapter,
            rejectionNotificationDelegate
          ),
          serviceRegistry(
            dsl.tweetAdapter,
            tweetContentDelegate
          )
        )
    }

  @Before
  def setUp(): Unit = {
    MockitoAnnotations.initMocks(this)
    Mocks.register("tweetAdapter", tweetContentDelegate)
    Mocks.register("emailAdapter", rejectionNotificationDelegate)
  }
/*
  @Test
  def testApprovedPath(): Unit =
    testCase("Happy Path")(
      startEvents.TweetWritten.step(StartInputs()),
      userTasks.ReviewTweet.step(TweetAproveInputs())
    ).run()
*/
  @Test
  @Deployment(
    resources = Array(
      "./example-twitter.bpmn",
      "static/" + createTweetFormPath,
      "static/" + reviewTweetFormPath
    )
  )
  def testApprovedPath2(): Unit =
    runTest(
      StartInputs(),
      TweetAproveInputs(),
      serviceTasks.PublishOnTwitterIdent,
      serviceTasks.SendRejectionNotificationIdent
    )

  @Test
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
    BpmnAwareTests.complete(task(), tweetAproveInputs.asJavaVars())
    assertThat(processInstance)
      .isEnded()
      .hasPassed(serviceHasPassedIdent)
      .hasNotPassed(serviceHasNotPassedIdent)

  private def startProcess(tweet: StartInputs) =
    runtimeService.startProcessInstanceByKey(
      TwitterDemoProcess.ident.toString,
      tweet.asJavaVars()
    )
