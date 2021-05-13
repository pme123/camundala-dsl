package camundala.examples.twitter

import camundala.bpmn.*
import camundala.dsl.DSL
import org.camunda.bpm.spring.boot.example.twitter.*
import org.camunda.bpm.spring.boot.example.twitter.delecateExpr.{emailAdapter, tweetAdapter}

import java.io.File

object TwitterProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("./examples/twitter/bpmn/cawemo/twitter-cawemo.bpmn"),
        twitterProcess.twitterBpmn,
        path("./examples/twitter/server/src/main/resources/twitter-process.bpmn")
      )
    ).run()

object twitterProcess extends DSL:

  private val kpiRatio = "KPI-Ratio"
  private val probability = "probability"
  lazy val twitterBpmn =
    bpmn("bpmns/with-ids/twitter-cawemo.bpmn")
      .processes(
        process("TwitterDemoProcess")
          .nodes(
            startEvent("TweetWritten")
              .staticForm("forms/createTweet.html")
              .prop("KPI-Cycle-Start", "Tweet Approval Time"),
            userTask("ReviewTweet")
              .staticForm("forms/reviewTweet.html")
              .prop("durationMean", "10000")
              .prop("durationSd", "5000"),
            serviceTask("SendRejectionNotification")
              .emailDelegate
              .prop(kpiRatio, "Tweet Rejected"),
            serviceTask("PublishOnTwitter")
              .tweetDelegate
              .prop(kpiRatio, "Tweet Approved"),
            exclusiveGateway("Approved")
              .prop("KPI-Cycle-End", "Tweet Approval Time"),
            exclusiveGateway("Join"),
            endEvent("TweetHandled")
          )
          .flows(
            sequenceFlow("SequenceFlow_4_SendRejectionNotification-Join"),
            sequenceFlow("No_Approved-SendRejectionNotification")
              .expression("!approved")
              .prop(probability, "13"),
            sequenceFlow("Yes_Approved-PublishOnTwitter")
              .expression("approved")
              .prop(probability, "87"),
            sequenceFlow("SequenceFlow_5_Join-TweetHandled"),
            sequenceFlow("SequenceFlow_3_PublishOnTwitter-Join"),
            sequenceFlow("SequenceFlow_9_TweetWritten-ReviewTweet"),
            sequenceFlow("SequenceFlow_2_ReviewTweet-Approved")
          )
      )
