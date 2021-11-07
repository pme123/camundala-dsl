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
  val processId = "TwitterDemoProcess"
  override implicit def tenantId: Option[String] = Some("{{tenantId}}")

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

  lazy val standardSample: CreateTweet = CreateTweet()
  private val descr =
    s"""This runs the Twitter Approvement Process.
       |""".stripMargin

  lazy val processApi =
    ProcessApi(processId)
      .startProcessInstance(
        processDefinitionKey = processId,
        name = processId,
        descr = Some(descr),
        inExamples = standardSample
      )
      .userTask(
        name = "Review Tweet",
        formExamples = NoInput(),
        completeExamples = Map(
          "Tweet accepted" -> ReviewTweet(),
          "Tweet rejected" -> ReviewTweet(false)
        )
      )
