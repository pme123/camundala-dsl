package camundala
package api

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import sttp.model.*
import sttp.tapir.{Endpoint, EndpointOutput, Schema}
import sttp.tapir.generic.auto.*

import java.util.Base64

trait EndpointDSL extends ApiErrorDSL, ApiInputDSL:
  implicit def tenantId: Option[String] = None

  def startProcessInstance[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](name: String, tag: String) =
    StartProcessInstance[In, Out](
      CamundaRestApi(name, tag, requestErrorOutputs = standardErrors)
    )

  def getActiveTask(name: String, tag: String) =
    GetActiveTask(
      CamundaRestApi[NoInput, NoOutput](
        name,
        tag,
        requestErrorOutputs = List(badRequest)
      )
    ).withInExample(NoInput())
      .withOutExample(NoOutput())

  def getTaskFormVariables[
      Out <: Product: Encoder: Decoder: Schema
  ](name: String, tag: String) =
    GetTaskFormVariables[Out](
      CamundaRestApi(name, tag, requestErrorOutputs = List(badRequest))
    ).withInExample(NoInput())

  def completeTask[
      In <: Product: Encoder: Decoder: Schema
  ](name: String, tag: String) =
    CompleteTask[In, NoOutput](
      CamundaRestApi(
        name,
        tag,
        requestErrorOutputs = List(badRequest, serverError)
      )
    ).withOutExample(NoOutput())

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

 /* extension [
      Out <: Product: Encoder: Decoder: Schema,
      T <: ApiEndpoint[NoInput, Out, T]
  ](endpoint: GetTaskFormVariables[Out])
    def outExample(example: Out): T =
      endpoint
        .copy(pathDescr =
          endpoint
            .copy(resultVariables = example.productElementNames.mkString(","))
        )
        .withOutExample(example)
  end extension
*/
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
