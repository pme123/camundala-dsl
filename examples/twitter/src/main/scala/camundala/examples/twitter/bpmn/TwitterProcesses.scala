package camundala
package examples.twitter
package bpmn

import java.util
import collection.JavaConverters.*
import camundala.examples.twitter.bpmn.TwitterApi.{CreateTweet, ReviewTweet}
import camundala.model.{BpmnsConfig, InOutObject}

object TwitterProcesses extends ProjectDSL:

  final val cawemoFolder = "./examples/twitter/cawemo"

  final val withIdFolder = "./examples/twitter/cawemo/with-ids"

  final val generatedFolder = "./examples/twitter/src/main/resources"

  val config: BpmnsConfig = bpmnsConfig
    .bpmns(
      bpmns.example__twitter
    )

  object bpmns:

    val example__twitter = bpmn("example__twitter")
      .processes(
        processes.TwitterDemoProcess
      )

    object processes:

      val TwitterDemoProcess = process("TwitterDemoP")
        .nodes(
          startEvents.TweetWritten,
          userTasks.ReviewTweet,
          exclusiveGateways.Approved,
          serviceTasks.SendRejectionNotification,
          serviceTasks.PublishOnTwitter,
          endEvents.TweetHandled
        )
        .flows(
          flows.No__Approved__SendRejectionNotification,
          flows.Yes__Approved__PublishOnTwitter,
        )
        .input(CreateTweet())
        .output(ReviewTweet())

      object userTasks:

        val ReviewTweetIdent = "ReviewTweetUT"

        lazy val ReviewTweet: UserTask =
          userTask(ReviewTweetIdent) //
            .dueDate("2021-12-31T12:23:00")
            .reviewTweetForm
            .prop("durationMean", "10000")
            .prop("durationSd", "5000")

      end userTasks

      object endEvents:

        val TweetHandledIdent = "TweetHandledEE"

        lazy val TweetHandled = endEvent(TweetHandledIdent)

      end endEvents

      object serviceTasks:

        val SendRejectionNotificationIdent = "SendRejectionNotificationST"

        lazy val SendRejectionNotification =
          serviceTask(SendRejectionNotificationIdent) //
            .emailDelegate
            .kpiRatio("Tweet Rejected")

        val PublishOnTwitterIdent = "PublishOnTwitterST"

        lazy val PublishOnTwitter =
          serviceTask(PublishOnTwitterIdent) //
            .tweetDelegate
            .kpiRatio("Tweet Approved")

      end serviceTasks

      object startEvents:

        val TweetWrittenIdent = "TweetWrittenSE"
        case class TweetWrittenOut(content: String = "Hello there")
            extends InOutObject

        lazy val TweetWritten =
          startEvent(TweetWrittenIdent) //
            .createTweetForm
            //   .outputs(TweetWrittenOut())
            .prop("KPI-Cycle-Start", "Tweet Approval Time")

      end startEvents

      object exclusiveGateways:

        val ApprovedIdent = "ApprovedEG"

        lazy val Approved =
          exclusiveGateway(ApprovedIdent)
            .prop("KPI-Cycle-End", "Tweet Approval Time")

      end exclusiveGateways

      object flows:

        val No__Approved__SendRejectionNotificationIdent =
          "NoSF__ApprovedEG__SendRejectionNotificationST"

        lazy val No__Approved__SendRejectionNotification =
          sequenceFlow(No__Approved__SendRejectionNotificationIdent) //
            .expression("!approved")
            .probability(13)

        val Yes__Approved__PublishOnTwitterIdent =
          "YesSF__ApprovedEG__PublishOnTwitterST"

        lazy val Yes__Approved__PublishOnTwitter =
          sequenceFlow(Yes__Approved__PublishOnTwitterIdent) //
            .expression("approved")
            .probability(87)

      end flows
    end processes
  end bpmns

end TwitterProcesses
