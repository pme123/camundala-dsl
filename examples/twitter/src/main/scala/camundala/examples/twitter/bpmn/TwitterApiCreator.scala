package camundala
package examples.twitter
package bpmn

import camundala.api.*
import api.UserTask
import api.endpoints.*
import camundala.examples.twitter.bpmn.TwitterApi.*
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import laika.api.*
import laika.ast.MessageFilter
import laika.format.*
import laika.markdown.github.GitHubFlavor
import os.*
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.generic.auto.*
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}
import sttp.tapir.{Endpoint, Schema}

object TwitterApiCreator extends APICreator {

  def title = "Twitter Process API"

  def version = "1.0"

  override lazy val serverPort = 8887

  override def basePath: Path = pwd / "examples" / "twitter"

  def apiEndpoints: Seq[ApiEndpoints] =
    Seq(
      twitterDemoProcess
        .endpoints(
          reviewTweetUT.endpoint
            .withOutExample("Tweet accepted", ReviewTweet())
            .withOutExample("Tweet rejected", ReviewTweet(false)),
        )
    )

}
