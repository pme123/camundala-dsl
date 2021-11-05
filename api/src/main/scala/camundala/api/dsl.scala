package camundala
package api

import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import sttp.model.*
import sttp.tapir.generic.auto.*
import sttp.tapir.{Endpoint, EndpointOutput, Schema}

import java.util.Base64

trait EndpointDSL extends ApiErrorDSL, ApiInputDSL:
  implicit def tenantId: Option[String] = None

  def endpoints(
      ePoints: (ApiEndpoint[_, _, _] | Seq[ApiEndpoint[_, _, _]])*
  ): Seq[ApiEndpoint[_, _, _]] =
    ePoints.toSeq.map {
      case p: ApiEndpoint[_, _, _] => Seq(p)
      case p: Seq[ApiEndpoint[_, _, _]] => p
    }.flatten

  def startProcessInstance[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      processDefinitionKey: String,
      name: Option[String] | String = None,
      tag: Option[String] | String = None,
      descr: Option[String] | String = None,
      inExamples: Map[String, In] | In = NoInput(),
      outExamples: Map[String, Out] | Out = NoOutput()
  ) =
    StartProcessInstance[In, Out](
      processDefinitionKey,
      camundaRestApi(
        name,
        tag,
        descr,
        examples(inExamples),
        examples(outExamples),
        requestErrorOutputs = standardErrors
      )
    )

  def userTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      name: Option[String] | String = None,
      tag: Option[String] | String = None,
      descr: Option[String] | String = None,
      formExamples: Map[String, In] | In,
      completeExamples: Map[String, Out] | Out
  ) =
    UserTaskEndpoint[In, Out](
      camundaRestApi(
        name,
        tag,
        descr,
        formExamples,
        completeExamples
      ),
      getActiveTask(name, tag, descr),
      getTaskFormVariables[In](name, tag, descr, formExamples),
      completeTask[Out](name, tag, descr, completeExamples)
    )

  private def camundaRestApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      name: Option[String] | String = None,
      tag: Option[String] | String = None,
      descr: Option[String] | String = None,
      inExamples: Map[String, In] | In = NoInput(),
      outExamples: Map[String, Out] | Out = NoOutput(),
      requestErrorOutputs: List[RequestErrorOutput] = Nil,
      businessKey: Option[String] = None
  ) =
    CamundaRestApi(
      name,
      tag,
      descr,
      RequestInput(examples(inExamples)),
      RequestOutput(StatusCode.Ok, examples(outExamples)),
      requestErrorOutputs,
      businessKey
    )

  private def examples[T](ex: Map[String, T] | T): Map[String, T] = ex match
    case eM: Map[String, T] => eM
    case e: T => Map("standard" -> e)

  private def getActiveTask(
      name: Option[String] | String = None,
      tag: Option[String] | String = None,
      descr: Option[String] | String = None
  ) =
    GetActiveTask(
      camundaRestApi(
        name,
        tag,
        descr,
        NoInput(),
        NoOutput(),
        List(badRequest)
      )
    )

  private def getTaskFormVariables[
      Out <: Product: Encoder: Decoder: Schema
  ](
      name: Option[String] | String = None,
      tag: Option[String] | String = None,
      descr: Option[String] | String = None,
      formExamples: Map[String, Out] | Out
  ) =
    GetTaskFormVariables[Out](
      camundaRestApi(
        name,
        tag,
        descr,
        NoInput(),
        formExamples,
        List(badRequest)
      )
    )

  private def completeTask[
      In <: Product: Encoder: Decoder: Schema
  ](
      name: Option[String] | String = None,
      tag: Option[String] | String = None,
      descr: Option[String] | String = None,
      completeExamples: Map[String, In] | In
  ) =
    CompleteTask[In](
      camundaRestApi(
        name,
        tag,
        descr,
        completeExamples,
        NoOutput(),
        List(badRequest, serverError)
      )
    )

  import reflect.Selectable.reflectiveSelectable
  def enumDescr(
      enumeration: { def values: Array[?] },
      descr: Option[String] = None
  ) =
    val enumDescription =
      s"Enumeration: \n- ${enumeration.values.mkString("\n- ")}"
    descr
      .map(_ + s"\n\n$enumDescription")
      .getOrElse(enumDescription)

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      T <: ApiEndpoint[In, Out, T]
  ](endpoint: ApiEndpoint[In, Out, T])

    def withName(n: String): T =
      endpoint.withName(n)

    def withTag(t: String): T =
      endpoint.withTag(t)

    def withDescr(description: String): T =
      endpoint.withDescr(description)

  end extension

end EndpointDSL

trait ApiInputDSL:

end ApiInputDSL

trait ApiErrorDSL:

  lazy val standardErrors = List(
    badRequest,
    notFound,
    serverError
  )

  def badRequest: RequestErrorOutput =
    error(StatusCode.BadRequest).example(
      CamundaError("BadRequest", defaultBadRequestMsg)
    )

  def notFound: RequestErrorOutput =
    error(StatusCode.NotFound).example(
      CamundaError("NotFound", defaultNotFoundMsg)
    )

  def serverError: RequestErrorOutput =
    error(StatusCode.InternalServerError).example(
      CamundaError("InternalServerError", defaultServerError)
    )

  def error(statusCode: StatusCode): RequestErrorOutput =
    RequestErrorOutput(statusCode)

  extension (request: RequestErrorOutput)
    def defaultExample: RequestErrorOutput =
      request.copy(examples = Map("defaultError" -> CamundaError()))

    def example(ex: CamundaError): RequestErrorOutput =
      request.copy(examples = request.examples + ("standardExample" -> ex))

    def example(
        `type`: String = "SomeExceptionClass",
        message: String = "a detailed message"
    ): RequestErrorOutput =
      request.copy(examples =
        request.examples + (`type` -> CamundaError(`type`, message))
      )
  end extension
  private val errorHandlingLink =
    s"[Introduction](https://docs.camunda.org/manual/$camundaVersion/reference/rest/overview/#error-handling)"
  private val defaultBadRequestMsg =
    s"The instance could not be created due to an invalid variable value, for example if the value could not be parsed to an Integer value or the passed variable type is not supported. See the $errorHandlingLink for the error response format."
  private val defaultNotFoundMsg =
    s"The instance could not be created due to a non existing process definition key. See the $errorHandlingLink for the error response format."
  private val defaultServerError =
    s"The instance could not be created successfully. See the $errorHandlingLink for the error response format."
end ApiErrorDSL
