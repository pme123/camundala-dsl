package camundala
package examples.twitter
package bpmn

import java.util
import collection.JavaConverters.*
import camundala.api
import camundala.model.InOutObject

object ExampleTwitter extends ProjectDSL:

  final val cawemoFolder = "./examples/twitter/cawemo"

  final val withIdFolder = "./examples/twitter/cawemo/with-ids"

  final val generatedFolder = "./examples/twitter/src/main/resources"

  val config = bpmnsConfig
    .bpmns(
      bpmns.example__twitter
    )

  object bpmns:

    val example__twitter = bpmn("example__twitter")
      .processes(
        processes.TwitterDemoProcess
      )

    object processes:

      val TwitterDemoProcess = process("TwitterDemoProcess")
        .nodes(
          startEvents.TweetWritten,
          userTasks.ReviewTweet,
          exclusiveGateways.Approved,
          serviceTasks.SendRejectionNotification,
          serviceTasks.PublishOnTwitter,
          endEvents.TweetHandled
        )
        .flows(
          flows.SequenceFlow_4__SendRejectionNotification__Join,
          flows.No__Approved__SendRejectionNotification,
          flows.Yes__Approved__PublishOnTwitter,
          flows.SequenceFlow_5__Join__TweetHandled,
          flows.SequenceFlow_3__PublishOnTwitter__Join,
          flows.SequenceFlow_9__TweetWritten__ReviewTweet,
          flows.SequenceFlow_2__ReviewTweet__Approved
        )
        .input(TwitterIn())
        .output(TwitterOut())

      object userTasks:

        val ReviewTweetIdent = "ReviewTweet"

        lazy val ReviewTweet: UserTask =
          userTask(ReviewTweetIdent) //
            .dueDate("2021-12-31T12:23:00")
            .reviewTweetForm
            .prop("durationMean", "10000")
            .prop("durationSd", "5000")

      end userTasks

      object endEvents:

        val TweetHandledIdent = "TweetHandled"

        lazy val TweetHandled = endEvent(TweetHandledIdent)

      end endEvents

      object serviceTasks:

        val SendRejectionNotificationIdent = "SendRejectionNotification"

        lazy val SendRejectionNotification =
          serviceTask(SendRejectionNotificationIdent) //
            .emailDelegate
            .kpiRatio("Tweet Rejected")

        val PublishOnTwitterIdent = "PublishOnTwitter"

        lazy val PublishOnTwitter =
          serviceTask(PublishOnTwitterIdent) //
            .tweetDelegate
            .kpiRatio("Tweet Approved")

      end serviceTasks

      object startEvents:

        val TweetWrittenIdent = "TweetWritten"
        case class TweetWrittenOut(content: String = "Hello there")
            extends InOutObject

        lazy val TweetWritten =
          startEvent(TweetWrittenIdent) //
            .createTweetForm
            //   .outputs(TweetWrittenOut())
            .prop("KPI-Cycle-Start", "Tweet Approval Time")

      end startEvents

      object exclusiveGateways:

        val ApprovedIdent = "Approved"

        lazy val Approved =
          exclusiveGateway(ApprovedIdent)
            .prop("KPI-Cycle-End", "Tweet Approval Time")

      end exclusiveGateways

      object flows:

        val SequenceFlow_4__SendRejectionNotification__JoinIdent =
          "SequenceFlow_4__SendRejectionNotification__Join"

        lazy val SequenceFlow_4__SendRejectionNotification__Join = sequenceFlow(
          SequenceFlow_4__SendRejectionNotification__JoinIdent
        )

        val No__Approved__SendRejectionNotificationIdent =
          "No__Approved__SendRejectionNotification"

        lazy val No__Approved__SendRejectionNotification =
          sequenceFlow(No__Approved__SendRejectionNotificationIdent) //
            .expression("!approved")
            .probability(13)

        val Yes__Approved__PublishOnTwitterIdent =
          "Yes__Approved__PublishOnTwitter"

        lazy val Yes__Approved__PublishOnTwitter =
          sequenceFlow(Yes__Approved__PublishOnTwitterIdent) //
            .expression("approved")
            .probability(87)

        val SequenceFlow_5__Join__TweetHandledIdent =
          "SequenceFlow_5__Join__TweetHandled"

        lazy val SequenceFlow_5__Join__TweetHandled = sequenceFlow(
          SequenceFlow_5__Join__TweetHandledIdent
        )

        val SequenceFlow_3__PublishOnTwitter__JoinIdent =
          "SequenceFlow_3__PublishOnTwitter__Join"

        lazy val SequenceFlow_3__PublishOnTwitter__Join = sequenceFlow(
          SequenceFlow_3__PublishOnTwitter__JoinIdent
        )

        val SequenceFlow_9__TweetWritten__ReviewTweetIdent =
          "SequenceFlow_9__TweetWritten__ReviewTweet"

        lazy val SequenceFlow_9__TweetWritten__ReviewTweet = sequenceFlow(
          SequenceFlow_9__TweetWritten__ReviewTweetIdent
        )

        val SequenceFlow_2__ReviewTweet__ApprovedIdent =
          "SequenceFlow_2__ReviewTweet__Approved"

        lazy val SequenceFlow_2__ReviewTweet__Approved = sequenceFlow(
          SequenceFlow_2__ReviewTweet__ApprovedIdent
        )
      end flows
    end processes
  end bpmns

end ExampleTwitter
