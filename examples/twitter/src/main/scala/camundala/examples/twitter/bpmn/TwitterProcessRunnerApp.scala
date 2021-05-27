package camundala.examples.twitter.bpmn

import camundala.dev.*

import camundala.dsl.DSL
import camundala.examples.twitter.services.*
import camundala.examples.twitter.dsl.*

import java.io.File

object TwitterProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("./examples/twitter/cawemo/twitter-cawemo.bpmn"),
        twitterExample.twitterBpmn,
        path(
          s"./examples/twitter/src/main/resources/${twitterExample.bpmnPath}"
        )
      )
    ).run()

object twitterExample extends DSL:

  case class TweetInputs(email: String = "me@myself.com",
                   content: String = "Test Tweet",
                   approved: Boolean = true) :

    val emailKey = "email"
    val contentKey = "content"
    val approvedKey = "approved"

  private val kpiRatio = "KPI-Ratio"
  final val bpmnPath = "twitter-process.bpmn"
  
  lazy val twitterBpmn =
    bpmn("./examples/twitter/cawemo/with-ids/twitter-cawemo.bpmn")
      .processes(
        twitterProcesss
      )

  val reviewTweetUserTaskIdent = "ReviewTweet"
  val publishOnTwitterIdent = "PublishOnTwitter"
  val sendRejectionNotificationIdent = "SendRejectionNotification"

  lazy val twitterProcesss = process("TwitterDemoProcess")
    .nodes(
      startEvent("TweetWritten") //
        .createTweetForm
        .prop("KPI-Cycle-Start", "Tweet Approval Time"),
      userTask(reviewTweetUserTaskIdent) //
        .reviewTweetForm
        .prop("durationMean", "10000")
        .prop("durationSd", "5000"),
      serviceTask(sendRejectionNotificationIdent) //
        .emailDelegate
        .kpiRatio("Tweet Rejected"),
      serviceTask(publishOnTwitterIdent) //
        .tweetDelegate
        .kpiRatio("Tweet Approved"),
      exclusiveGateway("Approved")
        .prop("KPI-Cycle-End", "Tweet Approval Time"),
      exclusiveGateway("Join"),
      endEvent("TweetHandled")
    )
    .flows(
      sequenceFlow(s"SequenceFlow_4_$sendRejectionNotificationIdent-Join"),
      sequenceFlow(s"No_Approved-$sendRejectionNotificationIdent")
        .expression("!approved")
        .probability(13),
      sequenceFlow(s"Yes_Approved-$publishOnTwitterIdent")
        .expression("approved")
        .probability(87),
      sequenceFlow("SequenceFlow_5_Join-TweetHandled"),
      sequenceFlow(s"SequenceFlow_3_$publishOnTwitterIdent-Join"),
      sequenceFlow("SequenceFlow_9_TweetWritten-ReviewTweet"),
      sequenceFlow("SequenceFlow_2_ReviewTweet-Approved")
    )
