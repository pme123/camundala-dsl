package camundala
package api

import io.circe.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.EndpointIO.Example
import sttp.tapir.EndpointOutput.Void
import sttp.tapir.Schema.annotations.description
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import io.circe.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.EndpointIO.Example
import sttp.tapir.EndpointOutput.Void
import sttp.tapir.Schema.annotations.description
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import cats.syntax.functor.*
import io.circe.{Decoder, Encoder}
import io.circe.syntax.*

trait EndpointDSL extends ApiErrorDSL, ApiInputDSL:
  implicit def tenantId: Option[String] = None

  /*
  def createPostmanEndpoint[In, Out, Err](
      name: String,
      descr: String,
      requestInput: RequestInput[In],
      requestOutput: RequestOutput[Out],
      requestErrorOutputs: List[RequestErrorOutput[Err]],
      businessKey: Option[String] = None
  )(implicit
      encoder: Encoder[In],
      decoder: Decoder[In],
      schema: Schema[In],
      encoderOut: Encoder[Out],
      decoderOut: Decoder[Out],
      schemaOut: Schema[Out],
      encoderErr: Encoder[Err],
      decoderErr: Decoder[Err],
      schemaErr: Schema[Err]
  ): Endpoint[_, _, _, _] =
    endpoint
      .name(s"Camunda: $name")
      .tag(name)
      .summary(s"Camunda: $name")
      .description(s"$descr")
      .in(postPath(name))
      .post
      .in(inMapper(requestInput, businessKey))
      .out(outMapper(requestOutput, businessKey))
      .errorOut(outputErrors(requestErrorOutputs))
   */
  case class StartProcessInstance[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      Err <: Product: Encoder: Decoder: Schema
  ](
      name: String,
      descr: String,
      requestInput: RequestInput[In],
      requestOutput: RequestOutput[Out] = RequestOutput.ok(NoOutput()),
      requestErrorOutputs: List[RequestErrorOutput[Err]] = List(
        badRequest,
        notFound,
        serverError
      ),
      businessKey: Option[String] = None
  ) extends ApiEndpoint:

    def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _] =
      baseEndpoint
        .in(postPath(name))
        .post
        .in(inMapper(requestInput, businessKey))
        .out(outMapper(requestOutput, businessKey))
        .errorOut(outputErrors(requestErrorOutputs))

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

  private def inMapper[In <: Product](
      requestInput: RequestInput[In],
      businessKey: Option[String]
  )(implicit encoder: Encoder[In], decoder: Decoder[In], schema: Schema[In]) =
    jsonBody[StartProcessIn[In]]
      .examples(requestInput.examples.map { case (name, ex) =>
        Example(
          StartProcessIn(ex, businessKey),
          Some(name),
          None
        )
      }.toList)

  private def outMapper[Out <: Product](
      outExamples: RequestOutput[Out],
      businessKey: Option[String]
  )(implicit
      encoder: Encoder[Out],
      decoder: Decoder[Out],
      schema: Schema[Out]
  ) =
    oneOf[StartProcessOut[Out]](
      oneOfMappingValueMatcher(
        outExamples.statusCode,
        jsonBody[StartProcessOut[Out]]
          .examples(outExamples.examples.map { case (name, ex: Out) =>
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

  private def outputErrors[Err](
      requests: List[RequestErrorOutput[Err]]
  )(implicit
      encoder: Encoder[Err],
      decoder: Decoder[Err],
      schema: Schema[Err]
  ): EndpointOutput[_ <: Err] =
    requests match
      case Nil =>
        Void()
      case x :: xs =>
        oneOf[Err](errMapper(x), xs.map(output => errMapper(output)): _*)

  private def errMapper[Err](output: RequestErrorOutput[Err])(implicit
      encoder: Encoder[Err],
      decoder: Decoder[Err],
      schema: Schema[Err]
  ): EndpointOutput.OneOfMapping[Err] =
    oneOfMappingValueMatcher(
      output.statusCode,
      jsonBody[Err].examples(output.examples.map { case (name, ex) =>
        Example(ex, Some(name), None)
      }.toList)
    ) { case _ =>
      true
    }

end EndpointDSL

sealed trait ApiEndpoint:
  def create()(implicit tenantId: Option[String]): Endpoint[_, _, _, _]
  def name: String
  def descr: String
  def baseEndpoint: Endpoint[_, _, _, _] =
    endpoint
      .name(s"${getClass.getSimpleName}: $name")
      .tag(name)
      .summary(s"${getClass.getSimpleName}: $name")
      .description(s"$descr")

end ApiEndpoint

trait ApiInputDSL:

  def inExample[In <: Product](
      in: In
  ): RequestInput[In] =
    RequestInput[In](in)

  def inExample[In <: Product](label: String, input: In) =
    RequestInput(Map(label -> input))

  extension [In <: Product: Encoder: Decoder: Schema](
      requestInput: RequestInput[In]
  )
    def example(label: String, input: In) =
      RequestInput(requestInput.examples + (label -> input))
  end extension
end ApiInputDSL

trait ApiErrorDSL:

  def badRequest: RequestErrorOutput[CamundaError] =
    error(StatusCode.BadRequest).example(
      CamundaError("BadRequest", defaultBadRequestMsg)
    )

  def notFound: RequestErrorOutput[CamundaError] =
    error(StatusCode.NotFound).example(
      CamundaError("BadRequest", defaultNotFoundMsg)
    )

  def serverError: RequestErrorOutput[CamundaError] =
    error(StatusCode.InternalServerError).example(
      CamundaError("InternalServerError", defaultServerError)
    )

  def error(statusCode: StatusCode): RequestErrorOutput[CamundaError] =
    RequestErrorOutput(statusCode)

  extension (request: RequestErrorOutput[CamundaError])
    def defaultExample: RequestErrorOutput[CamundaError] =
      request.copy(examples = Map("defaultError" -> CamundaError()))

    def example(ex: CamundaError): RequestErrorOutput[CamundaError] =
      request.copy(examples = request.examples + ("standardExample" -> ex))

    def example(
        `type`: String = "SomeExceptionClass",
        message: String = "a detailed message"
    ): RequestErrorOutput[CamundaError] =
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
