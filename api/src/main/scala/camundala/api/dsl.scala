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

trait ApiDSL extends
  EndpointDSL,
  ApiErrorDSL

trait EndpointDSL:
  def tenantId: Option[String] = None

  private def postPath(name: String) =
    val basePath = "process-definition" / "key" / processDefinitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
      .getOrElse(basePath / "start")

  private def processDefinitionKeyPath(name: String) =
    path[String]("key").description("The processDefinitionKey of the Process").example(name)

  private def tenantIdPath(id: String) =
    path[String]("tenant-id").description("The tenant, the process is deployed for.").example(id)

  extension (apiEndpoint: Endpoint[_, _, _, _])
    def descr(apiDescr: String) = apiEndpoint.description(apiDescr)

   // def body()
  end extension

  def createPostmanEndpoint[In, Out, Err](name: String,
                                          descr: String,
                                          requestInput: RequestInput[In],
                                          requestOutput: RequestOutput[Out],
                                          requestErrorOutputs: List[RequestErrorOutput[Err]],
                                          businessKey: Option[String] = None)
                                         (implicit encoder: Encoder[In], decoder: Decoder[In], schema: Schema[In],
                                          encoderOut: Encoder[Out], decoderOut: Decoder[Out], schemaOut: Schema[Out],
                                          encoderErr: Encoder[Err], decoderErr: Decoder[Err], schemaErr: Schema[Err]): Endpoint[_, _, _, _] =
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

  private def inMapper[In](requestInput: RequestInput[In], businessKey: Option[String])(
    implicit encoderIn: Encoder[In], decoderIn: Decoder[In], schemaIn: Schema[In]
  ) =
    jsonBody[StartProcessIn[In]]
      .examples(requestInput.examples
        .map { case (name, ex) =>
          Example(StartProcessIn[In](InVariables(InOutJson(ex)), businessKey), Some(name), None)
        }.toList)

  private def outMapper[Out](outExamples: RequestOutput[Out], businessKey: Option[String]
                            )(implicit encoderOut: Encoder[Out], decoderOut: Decoder[Out], schemaOut: Schema[Out]
                            ): EndpointOutput[StartProcessOut[Out]] = {
    oneOf[StartProcessOut[Out]](
      oneOfMappingValueMatcher(outExamples.statusCode,
        jsonBody[StartProcessOut[Out]]
          .examples(outExamples.examples.map { case (name, ex) => Example(StartProcessOut(businessKey = businessKey, variables = OutVariables(InOutJson(ex))), Some(name), None) }.toList)) {
        case _ => true
      }
    )
  }

  private def outputErrors[Err](requests: List[RequestErrorOutput[Err]]
                               )(implicit encoderO: Encoder[Err], decoderO: Decoder[Err], schemaO: Schema[Err]
                               ): EndpointOutput[_ <: Err] =
    requests match
      case Nil =>
        Void()
      case x :: xs =>
        oneOf[Err](
          errMapper(x),
          xs.map(output =>
            errMapper(output)
          ): _*)

  private def errMapper[Err](output: RequestErrorOutput[Err]
                            )(implicit encoderO: Encoder[Err], decoderO: Decoder[Err], schemaO: Schema[Err]
                            ): EndpointOutput.OneOfMapping[Err] = {
    oneOfMappingValueMatcher(output.statusCode,
      jsonBody[Err].examples(
        output.examples.map {
          case (name, ex) => Example(ex, Some(name), None)
        }.toList)) {
      case _ => true
    }
  }

end EndpointDSL

trait ApiErrorDSL:

  def badRequest: RequestErrorOutput[CamundaError] =
    error(StatusCode.BadRequest)

  def notFound: RequestErrorOutput[CamundaError] =
    error(StatusCode.NotFound)

  def serverError: RequestErrorOutput[CamundaError] =
    error(StatusCode.InternalServerError)

  def error(statusCode: StatusCode): RequestErrorOutput[CamundaError] =
    RequestErrorOutput(statusCode)

  extension (request: RequestErrorOutput[CamundaError])
    def defaultExample: RequestErrorOutput[CamundaError] =
      request.copy(examples = Map("defaultError" -> CamundaError()))

    def example(ex: CamundaError): RequestErrorOutput[CamundaError] =
      request.copy(examples = request.examples + ("standardExample" -> ex))

    def example(`type`: String = "SomeExceptionClass", message: String = "a detailed message"): RequestErrorOutput[CamundaError] =
      request.copy(examples = request.examples + (`type` -> CamundaError(`type`, message)))
  end extension
end ApiErrorDSL

