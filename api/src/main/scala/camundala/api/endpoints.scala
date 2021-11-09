package camundala
package api

import io.circe.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.{Endpoint, EndpointInput, EndpointOutput, path, query}

case class CamundaRestApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    name: Option[String] | String = None,
    tag: Option[String] | String = None,
    descr: Option[String] | String = None,
    requestInput: RequestInput[In] = RequestInput[In](),
    requestOutput: RequestOutput[Out] = RequestOutput[Out](),
    requestErrorOutputs: List[RequestErrorOutput] = Nil
):
  lazy val maybeName = name match
    case n: Option[String] => n
    case n: String => Some(n)

  lazy val maybeTag = tag match
    case t: Option[String] => t
    case t: String => Some(t)

  lazy val maybeDescr = descr match
    case d: Option[String] => d
    case d: String => Some(d)

  def outputErrors(): EndpointOutput[CamundaError] =
    requestErrorOutputs match
      case Nil =>
        Void()
      case x :: xs =>
        oneOf[CamundaError](
          errMapper(x),
          xs.map(output => errMapper(output)): _*
        )

  private def errMapper(
      output: RequestErrorOutput
  ): EndpointOutput.OneOfMapping[CamundaError] =
    oneOfMappingValueMatcher(
      output.statusCode,
      jsonBody[CamundaError].examples(output.examples.map { case (name, ex) =>
        Example(ex, Some(name), None)
      }.toList)
    ) { case _ =>
      true
    }

  def inMapper[T <: Product: Encoder: Decoder: Schema](
      createInput: (example: In) => T
  ): Option[EndpointInput[_]] =
    Some(
      jsonBody[T]
        .examples(requestInput.examples.map { case (label, ex) =>
          Example(
            createInput(ex),
            Some(label),
            None
          )
        }.toList)
    )

  lazy val noInputMapper: Option[EndpointInput[_]] =
    None

  def outMapper[
      T <: Product | Map[String, CamundaVariable] |
        Seq[Product | Map[String, CamundaVariable]]: Encoder: Decoder: Schema
  ](
      createOutput: (example: Out) => T
  ): Option[EndpointOutput[_]] =
    Some(
      oneOf[T](
        oneOfMappingValueMatcher(
          requestOutput.statusCode,
          jsonBody[T]
            .examples(requestOutput.examples.map { case (name, ex: Out) =>
              Example(
                createOutput(ex),
                Some(name),
                None
              )
            }.toList)
        ) { case _ =>
          true
        }
      )
    )
  lazy val noOutputMapper: Option[EndpointOutput[_]] =
    None

end CamundaRestApi

sealed trait ApiEndpoint[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: ApiEndpoint[In, Out, T]
] extends Product:
  def restApi: CamundaRestApi[In, Out]
  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]]
  lazy val name: String =
    s"${restApi.maybeName.getOrElse(inExample.getClass.getSimpleName)}: ${getClass.getSimpleName}"
  lazy val tag: String = restApi.maybeTag.getOrElse(name)
  def descr: String = restApi.maybeDescr.getOrElse("")
  lazy val inExample: In = restApi.requestInput.examples.values.head
  lazy val outExample: Out = restApi.requestOutput.examples.values.head
  def outStatusCode: StatusCode
  protected def inMapper(): Option[EndpointInput[_]]
  protected def outMapper(): Option[EndpointOutput[_]]

  def withRestApi(restApi: CamundaRestApi[In, Out]): T

  def withName(n: String): T =
    withRestApi(restApi.copy(name = Some(n)))

  def withTag(t: String): T =
    withRestApi(restApi.copy(tag = Some(t)))

  def withDescr(description: String): T =
    withRestApi(restApi.copy(descr = Some(description)))

  def withInExample(example: In): T =
    withRestApi(restApi.copy(requestInput = RequestInput(example)))

  def withInExample(label: String, example: In): T =
    withRestApi(
      restApi.copy(requestInput = restApi.requestInput :+ (label, example))
    )

  def withOutExample(example: Out): T =
    withRestApi(
      restApi.copy(requestOutput = RequestOutput(outStatusCode, example))
    )

  def withOutExample(label: String, example: Out): T =
    withRestApi(
      restApi.copy(requestOutput = restApi.requestOutput :+ (label, example))
    )

  def withOutExamples(examples: Map[String, Out]): T =
    withRestApi(
      restApi.copy(requestOutput = RequestOutput[Out](outStatusCode, examples))
    )

  def baseEndpoint: Endpoint[_, _, _, _] =
    Some(
      endpoint
        .name(name)
        .tag(tag)
        .summary(name)
        .description(descr)
        .errorOut(restApi.outputErrors())
    ).map(ep => inMapper().map(ep.in).getOrElse(ep))
      .map(ep => outMapper().map(ep.out).getOrElse(ep))
      .get

end ApiEndpoint

case class StartProcessInstance[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    processDefinitionKey: String,
    restApi: CamundaRestApi[In, Out]
) extends ApiEndpoint[In, Out, StartProcessInstance[In, Out]]:

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): StartProcessInstance[In, Out] =
    copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]] =
    Seq(
      baseEndpoint
        .in(postPath(processDefinitionKey))
        .post
    )

  private def postPath(name: String)(implicit tenantId: Option[String]) =
    val basePath =
      "process-definition" / "key" / definitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
      .getOrElse(basePath / "start") / s"--REMOVE:${restApi.name}--"

  protected def inMapper() =
    restApi.inMapper[StartProcessIn[In]] { (example: In) =>
      StartProcessIn(
        Some(example),
        CamundaVariable.toCamunda(example)
      )
    }

  protected def outMapper() =
    restApi.outMapper[StartProcessOut[Out]] { (example: Out) =>
      StartProcessOut(Some(example), CamundaVariable.toCamunda(example))
    }

  override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
    s"""
      |
      |Usage as _CallActivity_:
      |```
      |lazy val $name =
      |          callActivity("$processDefinitionKey") //TODO adjust to your CallActivity id!
      |            .calledElement("$processDefinitionKey")
      |            ${inSources}
      |            ${outSources}
      |```
      |""".stripMargin

  private lazy val inSources =
    inExample match
      case NoInput() => ""
      case _ =>
        inExample.productElementNames.mkString(""".inSource("""",
          """"
            |            .inSource("""".stripMargin, """")""")

  private lazy val outSources =
    outExample match
      case NoOutput() => ""
      case _ =>
        outExample.productElementNames.mkString(""".outSource("""",
          """"
            |            .outSource("""".stripMargin, """")""")

end StartProcessInstance

case class GetTaskFormVariables[
    Out <: Product: Encoder: Decoder: Schema
](
    restApi: CamundaRestApi[NoInput, Out]
) extends ApiEndpoint[NoInput, Out, GetTaskFormVariables[Out]]:

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[NoInput, Out]
  ): GetTaskFormVariables[Out] =
    copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]] =
    Seq(
      baseEndpoint
        .description(
          """Retrieves the form variables for a task.
            |The form variables take form data specified on the task into account.
            |If form fields are defined, the variable types and default values of the form fields are taken into account.""".stripMargin
        )
        .in(getPath)
        .in(
          query[String]("variableNames")
            .description(
              """A comma-separated list of variable names. Allows restricting the list of requested variables to the variable names in the list.
                |It is best practice to restrict the list of variables to the variables actually required by the form in order to minimize fetching of data. If the query parameter is ommitted all variables are fetched.
                |If the query parameter contains non-existent variable names, the variable names are ignored.""".stripMargin
            )
            .default(outExample.productElementNames.mkString(","))
        )
        .in(
          query[Boolean]("deserializeValues")
            .default(false)
        )
        .get
    )

  private lazy val getPath =
    "task" / taskIdPath() / "form-variables" / s"--REMOVE:${restApi.name}--"

  protected def inMapper() =
    restApi.noInputMapper

  protected def outMapper() = restApi.outMapper[Map[String, CamundaVariable]] {
    (example: Out) =>
      CamundaVariable.toCamunda(example)
  }

end GetTaskFormVariables

case class CompleteTask[
    In <: Product: Encoder: Decoder: Schema
](
    restApi: CamundaRestApi[In, NoOutput]
) extends ApiEndpoint[In, NoOutput, CompleteTask[In]]:

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[In, NoOutput]
  ): CompleteTask[In] =
    copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]] =
    Seq(
      baseEndpoint
        .in(postPath)
        .post
    )

  private lazy val postPath =
    "task" / taskIdPath() / "complete" / s"--REMOVE:${restApi.name}--"

  protected def inMapper() =
    restApi.inMapper[CompleteTaskIn[In]] { (example: In) =>
      CompleteTaskIn(Some(example), CamundaVariable.toCamunda(example))
    }

  protected def outMapper(): Option[EndpointOutput[_]] =
    restApi.noOutputMapper

end CompleteTask

case class GetActiveTask(
    restApi: CamundaRestApi[NoInput, NoOutput]
) extends ApiEndpoint[NoInput, NoOutput, GetActiveTask]:

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[NoInput, NoOutput]
  ): GetActiveTask = copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]] =
    Seq(
      baseEndpoint
        .in(postPath)
        .post
    )

  private lazy val postPath =
    "task" / s"--REMOVE:${restApi.name}--"

  protected def inMapper() =
    restApi.noInputMapper

  protected def outMapper() =
    restApi.noOutputMapper

end GetActiveTask

case class UserTaskEndpoint[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    restApi: CamundaRestApi[In, Out],
    getActiveTask: GetActiveTask,
    getTaskFormVariables: GetTaskFormVariables[In],
    completeTask: CompleteTask[Out]
) extends ApiEndpoint[In, Out, UserTaskEndpoint[In, Out]]:

  val outStatusCode = StatusCode.Ok //not used

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): UserTaskEndpoint[In, Out] =
    copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]] =
    val in = completeTask.restApi.copy(requestInput =
      RequestInput(restApi.requestOutput.examples)
    )
    val out = getTaskFormVariables.restApi.copy(requestOutput =
      RequestOutput(outStatusCode, restApi.requestInput.examples)
    )
    getActiveTask.create() ++
      getTaskFormVariables
        .withRestApi(out)
        .create() ++
      completeTask
        .withRestApi(in)
        .create()

  protected def inMapper() = ???

  protected def outMapper(): Option[EndpointOutput[_]] = ???

end UserTaskEndpoint

enum HitPolicy:

  case UNIQUE
  case FIRST
  case ANY
  case COLLECT
  case RULE_ORDER
end HitPolicy

case class EvaluateDecision[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    decisionDefinitionKey: String,
    hitPolicy: HitPolicy,
    restApi: CamundaRestApi[In, Out]
) extends ApiEndpoint[In, Out, EvaluateDecision[In, Out]]:

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): EvaluateDecision[In, Out] =
    copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Seq[Endpoint[_, _, _, _]] =
    Seq(
      baseEndpoint
        .in(postPath(decisionDefinitionKey))
        .post
    )

  private def postPath(name: String)(implicit tenantId: Option[String]) =
    val basePath =
      "decision-definition" / "key" / definitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "evaluate")
      .getOrElse(basePath / "evaluate") / s"--REMOVE:${restApi.name}--"

  import HitPolicy.*

  protected def inMapper() =
    restApi.inMapper[EvaluateDecisionIn[In]] { (example: In) =>
      EvaluateDecisionIn(
        Some(example),
        CamundaVariable.toCamunda(example)
      )
    }

  protected def outMapper() =
    hitPolicy match
      case UNIQUE | FIRST | ANY =>
        restApi.outMapper[Map[String, CamundaVariable]] { (example: Out) =>
          CamundaVariable.toCamunda(example)
        }
      case _ =>
        restApi.outMapper[Seq[Map[String, CamundaVariable]]] { (example: Out) =>
          Seq(CamundaVariable.toCamunda(example))
        }

end EvaluateDecision

private def definitionKeyPath(key: String) =
  path[String]("key")
    .description(
      "The Process- or Decision-DefinitionKey of the Process or Decision"
    )
    .default(key)

private def taskIdPath() =
  path[String]("taskId")
    .description("""The taskId of the Form.
                   |> This is the result id of the `GetActiveTask`
                   |""".stripMargin)
    .default("{{taskId}}")

private def tenantIdPath(id: String) =
  path[String]("tenant-id")
    .description("The tenant, the process is deployed for.")
    .default(id)
