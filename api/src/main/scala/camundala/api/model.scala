package camundala
package api

import io.circe.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.{Endpoint, EndpointInput, EndpointOutput, query, path}
//import sttp.tapir.EndpointIO.annotations.{query, path as pathParam, endpointInput}

import java.util.Base64
import scala.collection.immutable.HashMap

type ExampleName = String

case class RequestErrorOutput(
    statusCode: StatusCode,
    examples: Map[ExampleName, CamundaError] = Map.empty
)

case class CamundaError(
    `type`: String = "SomeExceptionClass",
    message: String = "a detailed message"
)

case class CamundaAuthError(
    private val `type`: String = "AuthorizationException",
    message: String = "a detailed message",
    userId: String = "jonny",
    permissionName: String = "DELETE",
    resourceName: String = "User",
    resourceId: String = "Mary"
)

sealed trait CamundaVariable

object CamundaVariable:
  /* import scala.language.implicitConversions
  implicit def toCString(v: String): CString = CString(v)
  implicit def toCInteger(v: Int): CInteger = CInteger(v)
  implicit def toCLong(v: Long): CLong = CLong(v)
  implicit def toCDouble(v: Double): CDouble = CDouble(v)
  implicit def toCBoolean(v: Boolean): CBoolean = CBoolean(v)
  implicit def toCEnum(v: String): CEnum = CEnum(v)
   */

  implicit val encodeCamundaVariable: Encoder[CamundaVariable] =
    Encoder.instance {
      case v: CString => v.asJson
      case v: CInteger => v.asJson
      case v: CLong => v.asJson
      case v: CDouble => v.asJson
      case v: CBoolean => v.asJson
      case v: CFile => v.asJson
      case v: CJson => v.asJson
      case v: CEnum => v.asJson
    }

  import reflect.Selectable.reflectiveSelectable

  def toCamunda[T <: Product: Encoder: Decoder: Schema](
      product: T
  ): Map[String, CamundaVariable] =
    product.productElementNames
      .zip(product.productIterator)
      .flatMap {
        case (k, v: String) =>
          Some(k -> CString(v))
        case (k, v: Int) =>
          Some(k -> CInteger(v))
        case (k, v: Long) =>
          Some(k -> CLong(v))
        case (k, v: Boolean) =>
          Some(k -> CBoolean(v))
        case (k, v: Float) =>
          Some(k -> CDouble(v.toDouble))
        case (k, v: Double) =>
          Some(k -> CDouble(v))
        case (k, f @ FileInOut(fileName, _, mimeType)) =>
          Some(
            k -> CFile(
              f.contentAsBase64,
              CFileValueInfo(
                fileName,
                mimeType
              )
            )
          )
        case (k, v: { def values: Array[?] }) =>
          Some(k -> CEnum(v.toString))
        case (k, v: Product) =>
          Some(
            k -> CJson(
              product.asJson.hcursor
                .downField(k)
                .as[Json]
                .toOption
                .map(_.toString)
                .getOrElse(s"$k -> v could NOT be Parsed to a JSON!")
            )
          )

      }
      .toMap

  case class CString(value: String, private val `type`: String = "String")
      extends CamundaVariable
  case class CInteger(value: Int, private val `type`: String = "Integer")
      extends CamundaVariable
  case class CLong(value: Long, private val `type`: String = "Long")
      extends CamundaVariable
  case class CBoolean(value: Boolean, private val `type`: String = "Boolean")
      extends CamundaVariable

  case class CDouble(value: Double, private val `type`: String = "Double")
      extends CamundaVariable

  case class CFile(
      @description("The File's content as Base64 encoded String.")
      value: String,
      valueInfo: CFileValueInfo,
      private val `type`: String = "File"
  ) extends CamundaVariable

  case class CFileValueInfo(
      filename: String,
      mimetype: Option[String]
  )

  case class CEnum(value: String, private val `type`: String = "String")
      extends CamundaVariable

  case class CJson(value: String, private val `type`: String = "Json")
      extends CamundaVariable

case class NoInput()

case class FileInOut(
    fileName: String,
    @description("The content of the File as a Byte Array.")
    content: Array[Byte],
    mimeType: Option[String]
):
  lazy val contentAsBase64 = Base64.getEncoder.encodeToString(content)

@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class StartProcessIn[T <: Product](
    _api_doc: Option[T],
    // use the description of the object
    variables: Map[String, CamundaVariable],
    @description("The business key of the process instance.")
    businessKey: Option[String] = None,
    @description("Set to false will not return the Process Variables.")
    withVariablesInReturn: Boolean = true
)

/*
@endpointInput("task/{taskId}/form-variables")
case class GetTaskFormVariablesPath(
                                    @pathParam
                                    taskId: String = "{{taskId}}",
                                    @query
                                    variableNames: Option[String] = None,
                                    @query
                                    deserializeValues: Boolean = true
                                  )
 */
@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class CompleteTaskIn[T <: Product](
    _api_doc: Option[T],
    // use the description of the object
    variables: Map[String, CamundaVariable],
    @description(
      "Set to false will not return the Process Variables and the Result Status is 204."
    )
    withVariablesInReturn: Boolean = true
)

@description(
  "A JSON object with the following properties"
)
case class GetActiveTaskIn(
    @description(
      """The id of the process - you want to get the active tasks.
        |> This is the result id of the `StartProcessOut`""".stripMargin
    )
    processInstanceId: String = "{{processInstanceId}}",
    @description("We are only interested in the active Task(s)")
    active: Boolean = true
)

case class RequestInput[T <: Product](examples: Map[String, T]):
  def :+(label: String, example: T) =
    copy(examples = examples + (label -> example))

object RequestInput:
  def apply[T <: Product](example: T) =
    new RequestInput[T](Map("standard" -> example))

  def apply[T <: Product]() =
    new RequestInput[T](Map.empty)

case class RequestOutput[T <: Product](
    statusCode: StatusCode,
    examples: Map[String, T]
):
  def :+(label: String, example: T) =
    copy(examples = examples + (label -> example))

object RequestOutput {

  def apply[Out <: Product](): RequestOutput[Out] =
    RequestOutput(StatusCode.Ok, Map.empty)

  def apply[Out <: Product](
      statusCode: StatusCode,
      example: Out
  ): RequestOutput[Out] =
    RequestOutput(statusCode, Map("standard" -> example))

  def ok[Out <: Product](example: Out): RequestOutput[Out] =
    apply(StatusCode.Ok, example)

  def created[Out <: Product](example: Out): RequestOutput[Out] =
    apply(StatusCode.Created, example)

  def ok[Out <: Product](examples: Map[ExampleName, Out]): RequestOutput[Out] =
    RequestOutput(StatusCode.Ok, examples)

  def created[Out <: Product](
      examples: Map[ExampleName, Out]
  ): RequestOutput[Out] =
    RequestOutput(StatusCode.Created, examples)

}

case class NoOutput()

@description(
  """A JSON object representing the newly created process instance.
    |""".stripMargin
)
case class StartProcessOut[T <: Product](
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: T,
    @description(
      """The id of the process instance.
        |
        |> **Postman**:
        |>
        |> Add the following to the tests to set the `processInstanceId`:
        |>
        |>```
        |let processInstanceId = pm.response.json().id
        |console.log("processInstanceId: " + processInstanceId)
        |pm.collectionVariables.set("processInstanceId", processInstanceId)
        |>```
        |""".stripMargin
    )
    id: String = "f150c3f1-13f5-11ec-936e-0242ac1d0007",
    @description("The id of the process definition.")
    definitionId: String =
      "processDefinitionKey:1:6fe66514-12ea-11ec-936e-0242ac1d0007",
    @description("The business key of the process instance.")
    businessKey: Option[String] = None
)

@description("A JSON object representing the newly created process instance.")
case class CompleteTaskOut[T <: Product](
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: T
)

@description("A JSON object representing the newly created process instance.")
case class GetActiveTaskOut(
    @description(
      """The Task Id you need to complete Task
        |
        |> **Postman**:
        |>
        |> Add the following to the tests to set the `taskId`:
        |>
        |>```
        |let taskId = pm.response.json()[0].id
        |console.log("taskId: " + taskId)
        |pm.collectionVariables.set("taskId", taskId)
        |>```
        |>
        |> This returns an Array!
        |""".stripMargin
    )
    id: String = "f150c3f1-13f5-11ec-936e-0242ac1d0007"
)

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
  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _]
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

  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _] =
    baseEndpoint
      .in(postPath(name))
      .post

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

  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _] =
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
    Out <: Product: Encoder: Decoder: Schema
](
    restApi: CamundaRestApi[In, Out]
) extends ApiEndpoint[In, Out, CompleteTask[In, Out]]:

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): CompleteTask[In, Out] =
    copy(restApi = restApi)

  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _] =
    baseEndpoint
      .in(postPath)
      .post

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

  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _] =
    baseEndpoint
      .in(postPath)
      .post

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
