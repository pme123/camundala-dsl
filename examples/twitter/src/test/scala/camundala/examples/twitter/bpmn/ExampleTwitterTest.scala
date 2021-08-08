package camundala
package examples.twitter
package bpmn

import DSL.Givens._
import ExampleTwitter.bpmns.processes._
import ExampleTwitter.{StartInputs, TweetAproveInputs}
import services.{RejectionNotificationDelegate, TweetContentOfflineDelegate}
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import org.mockito.Mockito._
import camundala.examples.twitter.bpmn.ExampleTwitter.bpmns.example__twitter
import camundala.test.{BpmnProcessTester, TestDSL, TestHelper}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests

class ExampleTwitterTest extends TestHelper, ProjectDSL, TestDSL:

  val bpmnsConfigToTest = ExampleTwitter.config
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
            mock(classOf[RejectionNotificationDelegate])
          ),
          serviceRegistry(
            dsl.tweetAdapter,
            mock(classOf[TweetContentOfflineDelegate])
          )
        )
    }
  /*  .cases(
        testCase("Happy Path")(
          // testStep("")
        )
      )*/

  @Rule
  def processEngineRule = new ProcessEngineRule

  @Test
  def testApprovedPath(): Unit =
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
    val task = getTask(userTasks.ReviewTweetIdent)
    BpmnAwareTests.complete(task, tweetAproveInputs.asJavaVars())
    assertThat(processInstance)
      .isEnded()
      .hasPassed(serviceHasPassedIdent)
      .hasNotPassed(serviceHasNotPassedIdent)

  private def startProcess(tweet: StartInputs) =
    runtimeService.startProcessInstanceByKey(
      TwitterDemoProcess.ident.toString,
      tweet.asJavaVars()
    )

  private def getTask(id: String | Ident) =
    val taskId = id.toString
    val task = taskService.createTaskQuery.active().singleResult
    println(s"TASK: $task")
    task
