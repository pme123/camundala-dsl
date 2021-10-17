package camundala
package api

import io.circe.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.{Endpoint, EndpointOutput}

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
  import scala.language.implicitConversions
  implicit def toCString(v: String): CString = CString(v)
  implicit def toCInteger(v: Int): CInteger = CInteger(v)
  implicit def toCLong(v: Long): CLong = CLong(v)
  implicit def toCFloat(v: Float): CFloat = CFloat(v)
  implicit def toCDouble(v: Double): CDouble = CDouble(v)
  implicit def toCBoolean(v: Boolean): CBoolean = CBoolean(v)

  implicit val encodeCamundaVariable: Encoder[CamundaVariable] =
    Encoder.instance {
      case v: CString => v.asJson
      case v: CInteger => v.asJson
      case v: CLong => v.asJson
      case v: CFloat => v.asJson
      case v: CDouble => v.asJson
      case v: CBoolean => v.asJson
    }

  // nit in Use
  def toCamunda(product: Product): Map[String, CamundaVariable] =
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
          Some(k -> CFloat(v))
        case (k, v: Double) =>
          Some(k -> CDouble(v))
        case other =>
          println(s"Not supported: $other")
          None
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
  case class CFloat(value: Float, private val `type`: String = "Float")
      extends CamundaVariable
  case class CDouble(value: Double, private val `type`: String = "Double")
      extends CamundaVariable

case class NoInput()

@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class StartProcessIn[T <: Product](
    // use the description of the object
    variables: T,
    @description("The business key of the process instance.")
    businessKey: Option[String] = None,
    @description("Set to false will not return the Process Variables.")
    withVariablesInReturn: Boolean = true
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
@description("A JSON object representing the newly created process instance.")
case class StartProcessOut[T <: Product](
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: T,
    @description("The id of the process instance.")
    id: String = "f150c3f1-13f5-11ec-936e-0242ac1d0007",
    @description("The id of the process definition.")
    definitionId: String =
      "processDefinitionKey:1:6fe66514-12ea-11ec-936e-0242ac1d0007",
    @description("The business key of the process instance.")
    businessKey: Option[String] = None
)

case class CamundaRestApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    name: String,
    descr: Option[String] = None,
    requestInput: RequestInput[In] = RequestInput[In](),
    requestOutput: RequestOutput[Out] = RequestOutput[Out](),
    requestErrorOutputs: List[RequestErrorOutput] = Nil,
    businessKey: Option[String] = None
):
  def inMapper()(implicit
      encoder: Encoder[In],
      decoder: Decoder[In],
      schema: Schema[In]
  ) =
    jsonBody[StartProcessIn[In]]
      .examples(requestInput.examples.map { case (name, ex) =>
        Example(
          StartProcessIn(ex, businessKey),
          Some(name),
          None
        )
      }.toList)

  def outMapper()(implicit
      encoder: Encoder[Out],
      decoder: Decoder[Out],
      schema: Schema[Out]
  ) =
    oneOf[StartProcessOut[Out]](
      oneOfMappingValueMatcher(
        requestOutput.statusCode,
        jsonBody[StartProcessOut[Out]]
          .examples(requestOutput.examples.map { case (name, ex: Out) =>
            Example(
              StartProcessOut(
                businessKey = businessKey,
                variables = ex
              ),
              Some(name),
              None
            )
          }.toList)
      ) { case _ =>
        true
      }
    )
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
end CamundaRestApi

sealed trait ApiEndpoint[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: ApiEndpoint[In, Out, T]
] extends Product:
  def restApi: CamundaRestApi[In, Out]
  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _]
  def name: String = restApi.name
  def descr: Option[String] = restApi.descr
  def outStatusCode: StatusCode

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
    withRestApi(restApi.copy(requestOutput = RequestOutput(outStatusCode, example)))

  def withOutExample(label: String, example: Out): T =
    withRestApi(
      restApi.copy(requestOutput = restApi.requestOutput :+ (label, example))
    )

  def baseEndpoint: Endpoint[_, _, _, _] =
    endpoint
      .name(s"${getClass.getSimpleName}: $name")
      .tag(name)
      .summary(s"${getClass.getSimpleName}: $name")
      .description(descr.getOrElse(""))

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
      .in(restApi.inMapper())
      .out(restApi.outMapper())
      .errorOut(restApi.outputErrors())

  private def postPath(name: String)(implicit tenantId: Option[String]) =
    val basePath =
      "process-definition" / "key" / processDefinitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
      .getOrElse(basePath / "start")

end StartProcessInstance

private def processDefinitionKeyPath(name: String) =
  path[String]("key")
    .description("The processDefinitionKey of the Process")
    .example(name)

private def tenantIdPath(id: String) =
  path[String]("tenant-id")
    .description("The tenant, the process is deployed for.")
    .example(id)
