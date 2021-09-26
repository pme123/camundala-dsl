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


type ExampleName = String

case class RequestErrorOutput[Err](statusCode: StatusCode, examples: Map[ExampleName, Err] = Map.empty)

case class CamundaError(`type`: String = "SomeExceptionClass", message: String = "a detailed message")

@description(
  """The input is one object, as Camunda has a complex value definition.
    |Otherwise you would need to wrap each input attribute.
    |""".stripMargin)
case class InVariables[T](
                           @description("The Input as a Domain Object.")
                           inputJson: InOutJson[T],
                         )

@description(
  """The output is one object, as Camunda has a complex value definition.
    |Otherwise you would need to wrap each output attribute.
    |""".stripMargin)
case class OutVariables[T](
                            @description("The Output as a Domain Object.")
                            outputJson: InOutJson[T],
                          )

case class InOutJson[T](
                         // use the description of the object
                         value: T,
                         @description("This is always Json")
                         `type`: String = "Json",
                       )

/**
 * standard execution message (sync requests)
 */
@description("A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)")
case class StartProcessIn[T](
                              // use the description of the object
                              variables: InVariables[T],
                              @description("The business key of the process instance.")
                              businessKey: Option[String] = None,
                              @description("Set to false will not return the Process Variables.")
                              withVariablesInReturn: Boolean = true,
                            )

case class RequestInput[T](examples: Map[String, T])

object RequestInput {
  def apply[T](example: T) =
    new RequestInput[T](Map("standard" -> example))
}

case class RequestOutput[T](statusCode: StatusCode, examples: Map[String, T])

object RequestOutput {

  def apply[Out](statusCode: StatusCode, example: Out): RequestOutput[Out] =
    RequestOutput(statusCode, Map("standard" -> example))

  def ok[Out](example: Out): RequestOutput[Out] =
    apply(StatusCode.Ok, example)

  def created[Out](example: Out): RequestOutput[Out] =
    apply(StatusCode.Created, example)


  def ok[Out](examples: Map[ExampleName, Out]): RequestOutput[Out] =
    RequestOutput(StatusCode.Ok, examples)

  def created[Out](examples: Map[ExampleName, Out]): RequestOutput[Out] =
    RequestOutput(StatusCode.Created, examples)

}

@description("A JSON object representing the newly created process instance.")
case class StartProcessOut[T](
                               @description("The Process Variables - Be aware that returns everything stored in the Process - even Passwords")
                               variables: OutVariables[T],
                               @description("The id of the process instance.")
                               id: String = "f150c3f1-13f5-11ec-936e-0242ac1d0007",
                               @description("The id of the process definition.")
                               definitionId: String = "processDefinitionKey:1:6fe66514-12ea-11ec-936e-0242ac1d0007",
                               @description("The business key of the process instance.")
                               businessKey: Option[String] = None,
                             )

