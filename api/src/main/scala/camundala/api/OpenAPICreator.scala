package camundala
package api

import os.{Path, pwd, read}
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}

trait APICreator extends App, EndpointDSL:

  def basePath: Path = pwd
  def docOpenApi: Path = basePath / "openApi.yml"

  def title: String
  def serverPort = 8080
  def contact: Option[Contact] = None

  def createChangeLog(): String =
    val changeLogFile = basePath / "CHANGELOG.md"
    if (changeLogFile.toIO.exists())
      createChangeLog(read(changeLogFile))
    else
      "There is no CHANGELOG.md in the Package."

  def createReadme(): String =
    val readme = basePath / "README.md"
    if (readme.toIO.exists())
      read.lines(readme).tail.mkString("\n")
    else
      "There is no README.md in the Project."

  def description: Option[String] = Some(
    s"""
       |
       |Generated Open API:
       |* **openApi.yml**: Documentation of the Processes.
       |  With small adjustments this can be imported to Postman!
       |
       |>WARNING: This is an experimental way and not approved.
       |
       |${createReadme()}
       |
       |${createChangeLog()}
       |""".stripMargin
  )

  def version: String

  def servers = List(Server(s"http://localhost:$serverPort/engine-rest"))

  def info = Info(title, version, description, contact = contact)

  def apiEndpoints: Seq[ApiEndpoint[_, _, _]]

  def openApi: OpenAPI =
    openAPIDocsInterpreter
      .toOpenAPI(apiEndpoints.flatMap(_.create()), info)
      .servers(servers)

  lazy val openAPIDocsInterpreter = OpenAPIDocsInterpreter(docsOptions =
    OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None)
  )

  writeOpenApi(docOpenApi, openApi)

  def writeOpenApi(path: Path, api: OpenAPI): Unit =
    if (os.exists(path))
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")

  private def createChangeLog(changeLog: String): String =
    import laika.api._
    import laika.format._
    import laika.markdown.github.GitHubFlavor
    import laika.ast.MessageFilter

    val transformer = Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor)
      .withRawContent
      .strict
      .failOnMessages(MessageFilter.None)
      // .renderMessages(MessageFilter.Error)
      .build
    transformer.transform(changeLog) match
      case Right(value) => s"""
                              |<details>
                              |<summary><b>CHANGELOG</b></summary>
                              |<p>
                              |
                              |$value
                              |
                              |</p>
                              |</details>
                              |""".stripMargin
      case Left(value) =>
        println(s"Problem CHANGELOG: $value")
        s"""CHANGELOG.md could not be created!
           |
           |Use Format:
           |
           |## 0.19.0 - 2021-09-10
           |### Fixed
           |- Fixed missing git push for master branch.
           |
           |## 0.18.1 - 2021-09-08
           |### Fixed
           |- Problem with Status 200 was not handled properly.
           |
           |### Added
           |- Helper to create ProceedProcess object.
           |
           |Problem:
           |
           |$value""".stripMargin

end APICreator
