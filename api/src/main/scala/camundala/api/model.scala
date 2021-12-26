package camundala
package api

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.model.StatusCode
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.description
import sttp.tapir.generic.auto.*

import java.util.Base64

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
                                                       ): Map[ExampleName, CamundaVariable] =
    product.productElementNames
      .zip(product.productIterator)
      .flatMap {
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
        case (k, v: Any) =>
          Some(k -> valueToCamunda(v))
      }
      .toMap

  def valueToCamunda(value: Any): CamundaVariable =
    value match
      case v: String =>
        CString(v)
      case v: Int =>
        CInteger(v)
      case v: Long =>
        CLong(v)
      case v: Boolean =>
        CBoolean(v)
      case v: Float =>
        CDouble(v.toDouble)
      case v: Double =>
        CDouble(v)
      case v: scala.reflect.Enum =>
        CEnum(v.toString)

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

end CamundaVariable

case class FileInOut(
                      fileName: String,
                      @description("The content of the File as a Byte Array.")
                      content: Array[Byte],
                      mimeType: Option[String]
                    ):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class StartProcessIn(
                           // use the description of the object
                           variables: Map[ExampleName, CamundaVariable],
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
case class CompleteTaskIn(
                           // use the description of the object
                           variables: Map[ExampleName, CamundaVariable],
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
case class EvaluateDecisionIn(
                               // use the description of the object
                               variables: Map[ExampleName, CamundaVariable]
                             )

case class RequestInput[T](examples: Map[ExampleName, T]):
  def :+(label: String, example: T): RequestInput[T] =
    copy(examples = examples + (label -> example))
  lazy val noInput: Boolean =
    examples.isEmpty

object RequestInput:
  def apply[T <: Product](example: T) =
    new RequestInput[T](Map("standard" -> example))

  def apply[T <: Product]() =
    new RequestInput[T](Map.empty)

case class RequestOutput[T](
                             statusCode: StatusCode,
                             examples: Map[ExampleName, T]
                           ):
  lazy val noOutdput: Boolean =
    examples.isEmpty
  def :+(label: String, example: T): RequestOutput[T] =
    copy(examples = examples + (label -> example))

object RequestOutput {

  def apply[Out <: Product](): RequestOutput[Out] =
    new RequestOutput(StatusCode.Ok, Map.empty)

  def apply[Out <: Product](
                             statusCode: StatusCode,
                             example: Out
                           ): RequestOutput[Out] =
    RequestOutput(statusCode, Map("standard" -> example))

  def ok[Out <: Product](
                          example: Out
                        ): RequestOutput[Out] =
    apply(StatusCode.Ok, example)

  def created[Out <: Product](
                               example: Out
                             ): RequestOutput[Out] =
    apply(StatusCode.Created, example)

  def ok[Out <: Product](
                          examples: Map[ExampleName, Out]
                        ): RequestOutput[Out] =
    RequestOutput(StatusCode.Ok, examples)

  def created[Out <: Product](
                               examples: Map[ExampleName, Out]
                             ): RequestOutput[Out] =
    RequestOutput(StatusCode.Created, examples)

}

@description(
  """A JSON object representing the newly created process instance.
    |""".stripMargin
)
case class StartProcessOut(
                            @description(
                              "The Process Variables - Be aware that returns everything stored in the Process."
                            )
                            variables: Map[ExampleName, CamundaVariable],
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

