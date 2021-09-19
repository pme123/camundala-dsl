package camundala
package examples.twitter
package bpmn

import ExampleTwitter.bpmns.processes.*
import ExampleTwitter.{TweetAproveInputs, TwitterIn, TwitterOut}
import services.{RejectionNotificationDelegate, TweetContentOfflineDelegate}
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import org.mockito.Mockito.*
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
      startEvents.TweetWritten.start(TwitterIn()),
      userTasks.ReviewTweet.step(TweetAproveInputs()),
      serviceTasks.PublishOnTwitter.step(),
      endEvents.TweetHandled.finish(TwitterOut())
    )

  @Test
  def testRejectedPath(): Unit =
    testCase(
      startEvents.TweetWritten.start(TwitterIn()),
      userTasks.ReviewTweet.step(TweetAproveInputs(approved = false)),
      serviceTasks.SendRejectionNotification.step(),
      endEvents.TweetHandled.finish(
        TwitterIn(),
        TweetAproveInputs(approved = false)
      )
    )
