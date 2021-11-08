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
    businessKey: Option[String] = Some("example-businesskey"),
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

@description(
  "A JSON object with the following properties:"
)
case class EvaluateDecisionIn[T <: Product](
                                             _api_doc: Option[T],
                                             // use the description of the object
                                             variables: Map[String, CamundaVariable],
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
    _api_doc: Option[T],
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: Map[String, CamundaVariable],
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
    businessKey: Option[String] = Some("example-businesskey")
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
