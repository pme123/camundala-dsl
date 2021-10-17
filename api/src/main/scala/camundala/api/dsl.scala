package camundala
package api

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import sttp.tapir.{Endpoint, EndpointOutput}
import sttp.tapir.generic.auto.*

trait EndpointDSL extends ApiErrorDSL, ApiInputDSL:
  implicit def tenantId: Option[String] = None

  def startProcessInstance[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](name: String) =
    StartProcessInstance[In, Out](
      CamundaRestApi(name, requestErrorOutputs = standardErrors)
    )

  def completeTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](name: String) =
    CompleteTask[In, Out](
      CamundaRestApi(name, requestErrorOutputs = List(badRequest, serverError))
    )

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      T <: ApiEndpoint[In, Out, T]
  ](endpoint: ApiEndpoint[In, Out, T])
    def descr(description: String): T =
      endpoint.withDescr(description)

    def inExample(example: In): T =
      endpoint.withInExample(example)

    def inExample(label: String, example: In): T =
      endpoint.withInExample(label, example)

    def outExample(example: Out): T =
      endpoint.withOutExample(example)
    def outExample(label: String, example: Out): T =
      endpoint.withOutExample(label, example)

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
