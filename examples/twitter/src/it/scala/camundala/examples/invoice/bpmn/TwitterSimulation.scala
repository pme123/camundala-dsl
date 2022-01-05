package camundala.examples.invoice.bpmn

import camundala.api.{CamundaVariable, StartProcessIn}
import camundala.bpmn.*
import camundala.examples.twitter.bpmn.TwitterApi.*
import camundala.gatling.BasicSimulationRunner
import camundala.test.CustomTests
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*

// exampleTwitter/GatlingIt/testOnly *TwitterSimulation
class TwitterSimulation extends BasicSimulationRunner :

  override val serverPort = 8887
  simulate(
    processScenario("Twitter - Approved")(
      twitterDemoProcess.start(),
      reviewTweetApprovedUT.getAndComplete(),
      twitterDemoProcess.check()
    ),
    processScenario("Twitter - Not Approved")(
      twitterDemoProcess.start(),
      reviewTweetApprovedUT.withOut(ReviewTweet(false)).getAndComplete(),
      twitterDemoProcess.withOut(ReviewTweet(false)).check()
    )
  )

