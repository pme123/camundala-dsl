package camundala.examples.twitter.bpmn

import camundala.dev.*

import camundala.dsl.DSL
import camundala.examples.twitter.services.*
import camundala.examples.twitter.dsl.*

import java.io.File
import camundala.model.BpmnsConfig

object TwitterProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        "ExampleTwitter",
        path("./examples/twitter/cawemo"),
        path("./examples/twitter/cawemo/with-ids"),
        path(twitterExample.outputPath),
        twitterExample.twitterBpmnsConfig
      )
    ).run()

object twitterExample extends DSL:

  final val outputPath = "./examples/twitter/src/main/resources"

  lazy val twitterBpmnsConfig = bpmnsConfig
    .bpmns(twitterBpmn)

  case class TweetInputs(
      email: String = "me@myself.com",
      content: String = "Test Tweet",
      approved: Boolean = true
  ):

    val emailKey = "email"
    val contentKey = "content"
    val approvedKey = "approved"
  
  end TweetInputs
  
  private val kpiRatio = "KPI-Ratio"

  lazy val twitterBpmn =
    bpmn("twitter-process")
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
      sequenceFlow(s"SequenceFlow_4__${sendRejectionNotificationIdent}__Join"),
      sequenceFlow(s"No__Approved__$sendRejectionNotificationIdent")
        .expression("!approved")
        .probability(13),
      sequenceFlow(s"Yes__Approved__$publishOnTwitterIdent")
        .expression("approved")
        .probability(87),
      sequenceFlow("SequenceFlow_5__Join__TweetHandled"),
      sequenceFlow(s"SequenceFlow_3__${publishOnTwitterIdent}__Join"),
      sequenceFlow("SequenceFlow_9__TweetWritten__ReviewTweet"),
      sequenceFlow("SequenceFlow_2__ReviewTweet__Approved")
    )
