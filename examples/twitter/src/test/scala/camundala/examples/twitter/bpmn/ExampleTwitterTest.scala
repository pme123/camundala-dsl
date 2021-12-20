package camundala.examples.twitter.bpmn

import camundala.examples.twitter.bpmn.TwitterApi.*
import camundala.examples.twitter.bpmn.TwitterProcesses.bpmns.*
import camundala.examples.twitter.bpmn.TwitterProcesses.bpmns.processes.*
import camundala.examples.twitter.dsl
import camundala.examples.twitter.dsl.*
import camundala.examples.twitter.services.{
  RejectionNotificationDelegate,
  TweetContentOfflineDelegate
}
import camundala.test.{TestConfig, TestDsl, TestRunner}
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.{After, Before, Rule, Test}
import org.mockito.Mockito.*
import org.mockito.{Mock, MockitoAnnotations}

class ExampleTwitterTest extends TestRunner:

  lazy val config: TestConfig =
    testConfig
      .deployments(
        baseResource / example__twitter.path,
        formResource / "createTweet.html",
        formResource / "reviewTweet.html"
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

  @Test
  def testApprovedPath(): Unit =
    test(twitterDemoProcess)(
      reviewTweetApprovedUT
    )

  @Test
  def testRejectedPath(): Unit =
    test(twitterDemoProcess)(
      reviewTweetNotApprovedUT
    )
