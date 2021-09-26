package camundala
package examples.twitter
package bpmn

import TwitterProcesses.bpmns.processes.*
import TwitterProcesses.bpmns.*
import services.{RejectionNotificationDelegate, TweetContentOfflineDelegate}
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.{Mock, MockitoAnnotations}
import org.mockito.Mockito.*
import TwitterApi.*
import camundala.test.{BpmnProcessTester, TestDSL, TestHelper}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests

class ExampleTwitterTest extends TestHelper, ProjectDSL, TestDSL:

  val bpmnsConfigToTest = TwitterProcesses.config
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
      startEvents.TweetWritten.start(CreateTweet()),
      userTasks.ReviewTweet.step(ReviewTweet()),
      serviceTasks.PublishOnTwitter.step(),
      endEvents.TweetHandled.finish(ReviewTweet())
    )

  @Test
  def testRejectedPath(): Unit =
    testCase(
      startEvents.TweetWritten.start(CreateTweet()),
      userTasks.ReviewTweet.step(ReviewTweet(approved = false)),
      serviceTasks.SendRejectionNotification.step(),
      endEvents.TweetHandled.finish(
        CreateTweet(),
        ReviewTweet(approved = false)
      )
    )
