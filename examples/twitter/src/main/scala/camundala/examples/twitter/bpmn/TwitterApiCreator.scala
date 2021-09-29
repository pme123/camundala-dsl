package camundala
package examples.twitter
package bpmn

import camundala.api.*
import os.*
import sttp.tapir.Endpoint

object TwitterApiCreator extends APICreator {

  def title = "Twitter Process API"

  def version = "1.0"

  override lazy val serverPort = 8887

  override def description: Option[String] = super.description.map(
    _ +
      """
        |This example demonstrates how you can use a BPMN process and the Tweeter API to build a simple Twitter client.
        |
        |>This is the [original README](https://github.com/camunda/camunda-bpm-examples/tree/master/spring-boot-starter/example-twitter)
        |""".stripMargin
  )

  override def docOpenApi: Path = pwd / "examples" / "twitter" / "openApi.yml"

  def apiEndpoints: Seq[ApiEndpoint] = TwitterApi.apiEndpoints


}
