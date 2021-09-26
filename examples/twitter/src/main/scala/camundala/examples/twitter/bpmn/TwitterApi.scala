package camundala
package examples.twitter
package bpmn

import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.Schema.annotations.description
import sttp.tapir.generic.auto.*
import sttp.tapir.{Endpoint, Schema, SchemaType}
import api.*
import model.{InOutObject, NoInputsOutputs}

object TwitterApi extends ApiDSL:
  val name = "TwitterDemoProcess"
  override def tenantId: Option[String] = Some("MyTENANT")

  @description("Every employee may create a Tweet.")
  case class CreateTweet(
      @description("The email address of the creator")
      email: String = "me@myself.com",
      @description("The conten of the Tweet.")
      content: String = "Test Tweet"
  ) extends InOutObject

  @description("Every Tweet has to be accepted by the Boss")
  case class ReviewTweet(
      @description("If true, the Boss accepted the Tweet")
      approved: Boolean = true
  ) extends InOutObject

  lazy val standardSample: CreateTweet = CreateTweet()
  private val descr =
    s"""This runs the Twitter Approvement Process.
       |""".stripMargin

  lazy val apiEndpoints =
    Seq(
          createPostmanEndpoint(
         name,
         descr,
         RequestInput(CreateTweet()),
         RequestOutput.ok(NoInputsOutputs),
         List(
           badRequest.example(CamundaError("BadStuffHappened", "There is a real Problem.")),
           notFound.defaultExample,
           serverError.example("InternalServerError", "Check the Server Logs!")
         )
       )
    )
