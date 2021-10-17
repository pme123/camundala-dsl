package camundala
package examples.twitter
package bpmn

import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.Schema.annotations.description
import sttp.tapir.generic.auto.*
import sttp.tapir.{Endpoint, Schema, SchemaType}
import api.*
import api.CamundaVariable.*
import io.circe.{Decoder, Encoder}

object TwitterApi extends EndpointDSL:
  val name = "TwitterDemoProcess"
  override implicit def tenantId: Option[String] = Some("MyTENANT")

  @description("""Every employee may create a Tweet.
                 |
                 |- email:   The email address of the creator.
                 |- content: The content of the Tweet.
                 |""".stripMargin)
  case class CreateTweet(
      //@description("Variables cannot be described as it is only possible to have one description per type!")
      email: CString = "me@myself.com",
      content: CString = "Test Tweet"
  ) extends InOutObject

  @description("""Every Tweet has to be accepted by the Boss.""")
  case class ReviewTweet(
      @description("If true, the Boss accepted the Tweet")
      approved: CBoolean = true
  ) extends InOutObject

  lazy val standardSample: CreateTweet = CreateTweet()
  private val descr =
    s"""This runs the Twitter Approvement Process.
       |""".stripMargin

  lazy val apiEndpoints: Seq[ApiEndpoint[_, _, _]] =
    Seq(
      startProcessInstance[CreateTweet, NoOutput](name)
        .descr(descr)
        .inExample(CreateTweet())
    )
