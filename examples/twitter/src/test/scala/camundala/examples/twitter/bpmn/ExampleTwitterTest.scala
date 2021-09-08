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

  @Test
  def testApprovedPath(): Unit =
    testCase(
      startEvents.TweetWritten.start(StartInputs()),
      userTasks.ReviewTweet.step(TweetAproveInputs()),
      serviceTasks.PublishOnTwitter.step(),
      endEvents.TweetHandled.finish(StartInputs(), TweetAproveInputs())
    )

  @Test
  def testRejectedPath(): Unit =
    testCase(
      startEvents.TweetWritten.start(StartInputs()),
      userTasks.ReviewTweet.step(TweetAproveInputs(approved = false)),
      serviceTasks.SendRejectionNotification.step(),
      endEvents.TweetHandled.finish(
        StartInputs(),
        TweetAproveInputs(approved = false)
      )
    )
