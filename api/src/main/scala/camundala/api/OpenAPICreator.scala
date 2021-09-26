package camundala
package api

import os.{pwd, Path}
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}

trait APICreator extends App, ApiDSL :

  def docOpenApi: Path = pwd / "openApi.yml"

  def title: String

  def contact: Option[Contact] = None

  def description: Option[String] = Some(
    """Generated Open API:
      |* openApi.yml: Documentation of the Processes.
      |WARNING: This is an experimental way and not approved.
      |""".stripMargin)

  def version: String

  def servers = List(Server("http://localhost:8080/engine-rest"))

  def info = Info(title, version, description, contact = contact)

  def apiEndpoints: Seq[Endpoint[_, _, _, _]]

  def openApi: OpenAPI =
    openAPIDocsInterpreter
      .toOpenAPI(apiEndpoints, info)
      .servers(servers)

  lazy val openAPIDocsInterpreter = OpenAPIDocsInterpreter(docsOptions = OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None))

  writeOpenApi(docOpenApi, openApi)

  def writeOpenApi(path: Path, api: OpenAPI): Unit =
    if (os.exists(path))
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")


end APICreator

