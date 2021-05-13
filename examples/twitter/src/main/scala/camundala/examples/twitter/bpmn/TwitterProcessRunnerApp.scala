package camundala.examples.twitter.bpmn

import camundala.bpmn.*

import camundala.dsl.DSL
import camundala.examples.twitter.delegates.*
import camundala.examples.twitter.delegates.delecateExpr.{emailAdapter, tweetAdapter}

import java.io.File

object TwitterProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("./examples/twitter/cawemo/twitter-cawemo.bpmn"),
        twitterProcess.twitterBpmn,
        path("./examples/twitter/src/main/resources/twitter-process.bpmn")
      )
    ).run()

object twitterProcess extends DSL:

  private val kpiRatio = "KPI-Ratio"
  lazy val twitterBpmn =
    bpmn("./examples/twitter/cawemo/with-ids/twitter-cawemo.bpmn")
      .processes(
        process("TwitterDemoProcess")
          .nodes(
            startEvent("TweetWritten")
              .
              .prop("KPI-Cycle-Start", "Tweet Approval Time"),
            userTask("ReviewTweet")
              .reviewTweetForm
              .prop("durationMean", "10000")
              .prop("durationSd", "5000"),
            serviceTask("SendRejectionNotification")
              .emailDelegate
              .kpiRatio("Tweet Rejected"),
            serviceTask("PublishOnTwitter")
              .tweetDelegate
              .kpiRatio("Tweet Approved"),
            exclusiveGateway("Approved")
              .prop("KPI-Cycle-End", "Tweet Approval Time"),
            exclusiveGateway("Join"),
            endEvent("TweetHandled")
          )
          .flows(
            sequenceFlow("SequenceFlow_4_SendRejectionNotification-Join"),
            sequenceFlow("No_Approved-SendRejectionNotification")
              .expression("!approved")
              .probability(13),
            sequenceFlow("Yes_Approved-PublishOnTwitter")
              .expression("approved")
              .probability(87),
            sequenceFlow("SequenceFlow_5_Join-TweetHandled"),
            sequenceFlow("SequenceFlow_3_PublishOnTwitter-Join"),
            sequenceFlow("SequenceFlow_9_TweetWritten-ReviewTweet"),
            sequenceFlow("SequenceFlow_2_ReviewTweet-Approved")
          )
      )
