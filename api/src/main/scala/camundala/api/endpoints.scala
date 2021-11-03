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
    name: String,
    tag: String,
    descr: Option[String] = None,
    requestInput: RequestInput[In] = RequestInput[In](),
    requestOutput: RequestOutput[Out] = RequestOutput[Out](),
    requestErrorOutputs: List[RequestErrorOutput] = Nil,
    businessKey: Option[String] = None
):

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
      createInput: (example: In, businessKey: Option[String]) => T
  ): Option[EndpointInput[_]] =
    Some(
      jsonBody[T]
        .examples(requestInput.examples.map { case (label, ex) =>
          Example(
            createInput(ex, businessKey),
            Some(label),
            None
          )
        }.toList)
    )

  lazy val noInputMapper: Option[EndpointInput[_]] =
    None

  def outMapper[
      T <: Product | Map[String, CamundaVariable]: Encoder: Decoder: Schema
  ](
      createOutput: (example: Out, businessKey: Option[String]) => T
  ): Option[EndpointOutput[_]] =
    Some(
      oneOf[T](
        oneOfMappingValueMatcher(
          requestOutput.statusCode,
          jsonBody[T]
            .examples(requestOutput.examples.map { case (name, ex: Out) =>
              Example(
                createOutput(ex, businessKey),
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
  lazy val name: String = restApi.name
  lazy val tag: String = restApi.tag
  lazy val descr: Option[String] = restApi.descr
  lazy val inExample: In = restApi.requestInput.examples.values.head
  lazy val outExample: Out = restApi.requestOutput.examples.values.head
  def outStatusCode: StatusCode
  protected def inMapper(): Option[EndpointInput[_]]
  protected def outMapper(): Option[EndpointOutput[_]]

  def withRestApi(restApi: CamundaRestApi[In, Out]): T
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
        .name(s"$name: ${getClass.getSimpleName}")
        .tag(tag)
        .summary(s"$name: ${getClass.getSimpleName}")
        .description(descr.getOrElse(""))
        .errorOut(restApi.outputErrors())
    ).map(ep => inMapper().map(ep.in).getOrElse(ep))
      .map(ep => outMapper().map(ep.out).getOrElse(ep))
      .get

end ApiEndpoint

case class StartProcessInstance[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
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
        .in(postPath(name))
        .post
    )

  private def postPath(name: String)(implicit tenantId: Option[String]) =
    val basePath =
      "process-definition" / "key" / processDefinitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
      .getOrElse(basePath / "start" / s"--REMOVE:${restApi.name}--")

  protected def inMapper() =
    restApi.inMapper[StartProcessIn[In]] {
      (example: In, businessKey: Option[String]) =>
        StartProcessIn(
          Some(example),
          CamundaVariable.toCamunda(example),
          businessKey
        )
    }

  protected def outMapper() =
    restApi.outMapper[StartProcessOut[Out]] {
      (example: Out, businessKey: Option[String]) =>
        StartProcessOut(example, businessKey = businessKey)
    }

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
    (example: Out, _) =>
      CamundaVariable.toCamunda(example)
  }

end GetTaskFormVariables

case class CompleteTask[
    In <: Product: Encoder: Decoder: Schema,
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
    restApi.inMapper[CompleteTaskIn[In]] { (example: In, _) =>
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
    restApi.inMapper[GetActiveTaskIn] { (_, _) =>
      GetActiveTaskIn()
    }
  protected def outMapper() =
    restApi.outMapper[GetActiveTaskOut] { (_, _) =>
      GetActiveTaskOut()
    }
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
    val in = completeTask.restApi.copy(requestInput = RequestInput(restApi.requestOutput.examples))
    val out = getTaskFormVariables.restApi.copy(requestOutput = RequestOutput(outStatusCode, restApi.requestInput.examples))
        println(s"getTaskFormVariables: ${out}")
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

private def processDefinitionKeyPath(name: String) =
  path[String]("key")
    .description("The processDefinitionKey of the Process")
    .default(name)

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
