package camundala
package api

import api.pure.UserTask
import api.endpoints.*
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

trait APICreator extends App:

  def basePath: Path = pwd
  def docOpenApi: Path = basePath / "openApi.yml"
  implicit def tenantId: Option[String] = None

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

  //def processes: Seq[pure.Process[_ <: Product, _]]

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

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      T <: pure.InOut[In, Out, T]
    ](process: pure.Process[In, Out])
    def endpoints =
      Seq(
        StartProcessInstance(
          process.id,
          CamundaRestApi(
            process.inOutDescr,
            process.id,
            requestErrorOutputs = standardErrors
          )
        )
      )
  end extension

  extension [
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: pure.InOut[In, Out, T]
  ](userTask: pure.UserTask[In, Out])
    def endpoints: Seq[ApiEndpoint[_, _, _]] =
      Seq(
        GetActiveTask(
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.descr
          )
        ),
        GetTaskFormVariables[In](
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.descr,
         /*   requestOutput = RequestOutput(
              StatusCode.Ok,
              formExamples + ("standard" -> userTask.in)
            )*/
            // List.empty//standardErrors
          )
        ),
        CompleteTask[Out](
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.descr,
        /*    requestInput =
              RequestInput(completeExamples + ("standard" -> userTask.out))
         */   // List.empty //List(badRequest, serverError)
          )
        )
      )
  end extension
end APICreator

