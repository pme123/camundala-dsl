package camundala
package examples.twitter
package bpmn

import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import camundala.api.*

object TwitterApi extends PureDsl:
  implicit def tenantId: Option[String] = Some("{{tenantId}}")

  @description("""Every employee may create a Tweet.
                 |
                 |- email:   The email address of the creator.
                 |- content: The content of the Tweet.
                 |""".stripMargin)
  case class CreateTweet(
      //@description("Variables cannot be described as it is only possible to have one description per type!")
      email: String = "me@myself.com",
      content: String = "Test Tweet"
  ) extends InOutObject

  @description("""Every Tweet has to be accepted by the Boss.""")
  case class ReviewTweet(
      @description("If true, the Boss accepted the Tweet")
      approved: Boolean = true
  ) extends InOutObject

  val twitterDemoProcess =
    val processId = "TwitterDemoProcess"
    process(
      id = processId,
      descr = "This runs the Twitter Approvement Process.",
      in = CreateTweet()
    )
  val reviewTweetUT = userTask(
    id = "ReviewTweet",
    in = NoInput(),
    out = ReviewTweet()
  )
