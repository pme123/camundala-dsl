package camundala
package examples.twitter.bpmn

import camundala.examples.twitter.bpmn.TwitterApi.{TweetHandledEE, reviewTweetApprovedUT, reviewTweetNotApprovedUT, twitterDemoProcess}
import camundala.examples.twitter.bpmn.TwitterProcesses.bpmns.example__twitter
import camundala.examples.twitter.dsl
import camundala.examples.twitter.services.{RejectionNotificationDelegate, TweetContentOfflineDelegate}
import camundala.test.{CommonTesting, ScenarioRunner, TestConfig, TestDsl, TestRunner}
import org.junit.Test
import org.mockito.Mockito.mock

trait TwitterUnitTests extends CommonTesting:
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
      reviewTweetApprovedUT,
      TweetHandledEE
    )

  @Test
  def testRejectedPath(): Unit =
    test(twitterDemoProcess)(
      reviewTweetNotApprovedUT,
      TweetHandledEE
    )

class ExampleTwitterTest extends TestRunner, TwitterUnitTests

class ExampleTwitterScenario extends ScenarioRunner, TwitterUnitTests