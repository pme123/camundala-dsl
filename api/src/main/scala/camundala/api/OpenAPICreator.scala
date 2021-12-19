package camundala
package api

import bpmn.*
import api.endpoints.*
import camundala.bpmn.{DecisionDmn, InOut, Process, UserTask}
import io.circe.generic.auto.*
import laika.api.*
import laika.ast.MessageFilter
import laika.format.*
import laika.markdown.github.GitHubFlavor
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.generic.auto.*
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}

trait APICreator extends App:

  def basePath: Path = pwd
  def openApiPath: Path = basePath / "openApi.yml"
  def postmanOpenApiPath: Path = basePath / "postmanOpenApi.yml"
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

  def info(title: String) = Info(title, version, description, contact = contact)

  def apiEndpoints(apiEP: ApiEndpoints*) =
    writeOpenApi(openApiPath, openApi(apiEP))
    writeOpenApi(postmanOpenApiPath, postmanOpenApi(apiEP))

  def openApi(apiEP: Seq[ApiEndpoints]): OpenAPI =
    openAPIDocsInterpreter
      .toOpenAPI(apiEP.flatMap(_.create()), info(title))
      .servers(servers)

  def postmanOpenApi(apiEP: Seq[ApiEndpoints]): OpenAPI =
    openAPIDocsInterpreter
      .toOpenAPI(apiEP.flatMap(_.createPostman()), info(s"Postman: $title"))
      .servers(servers)

  lazy val openAPIDocsInterpreter = OpenAPIDocsInterpreter(docsOptions =
    OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None)
  )


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
                              |.some **CHANGELOG**
                              |[%collapsible]
                              |====
                              |$value
                              |====
                              |
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
      T <: InOut[In, Out, T]
  ](process: Process[In, Out])
    def endpoints(activities: ApiEndpoint[_, _, _]*) =
      ApiEndpoints(
        process.id,
        StartProcessInstance(
          process.id,
          CamundaRestApi(
            process.inOutDescr,
            process.id,
            requestErrorOutputs = startProcessInstanceErrors
          )
        ) +: activities
      )
  end extension

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](userTask: UserTask[In, Out])
    def endpoint: ApiEndpoint[In, Out, UserTaskEndpoint[In, Out]] =
      UserTaskEndpoint(
        CamundaRestApi(
          userTask.inOutDescr,
          userTask.id,
          Nil
        ),
        GetActiveTask(
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.descr,
            requestErrorOutputs = getActiveTaskErrors
          )
        ),
        GetTaskFormVariables[In](
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.descr,
            requestErrorOutputs = getTaskFormVariablesErrors
          )
        ),
        CompleteTask[Out](
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.descr,
            requestErrorOutputs = completeTaskErrors
          )
        )
      )
  end extension

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](dmn: DecisionDmn[In, Out])
    def endpoint: ApiEndpoint[In, Out, EvaluateDecision[In, Out]] =
      EvaluateDecision(
        dmn.decisionDefinitionKey,
        dmn.hitPolicy,
        CamundaRestApi(
          dmn.inOutDescr,
          dmn.id,
          evaluateDecisionErrors
        )
      )
  end extension

end APICreator
