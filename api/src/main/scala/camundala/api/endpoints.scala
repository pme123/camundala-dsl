package camundala
package api

import camundala.api.CamundaVariable.*
import camundala.bpmn.*
import camundala.domain.*
import io.circe.*
import io.circe.Json.JNumber
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.{Endpoint, EndpointInput, EndpointOutput}

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
  def :+(label: String, example: T) =
    copy(examples = examples + (label -> example))
  lazy val noInput =
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
  lazy val noOutdput =
    examples.isEmpty
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

object endpoints:

  case class CamundaRestApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      name: String,
      tag: String,
      descr: Option[String] | String = None,
      requestInput: RequestInput[In] = RequestInput[In](),
      requestOutput: RequestOutput[Out] = RequestOutput[Out](),
      requestErrorOutputs: List[RequestErrorOutput] = Nil
  ):

    lazy val maybeDescr = descr match
      case d: Option[String] => d
      case d: String => Some(d)

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
        createInput: (example: In) => T
    ): Option[EndpointInput[_]] =
      if (requestInput.noInput)
        None
      else
        Some(
          jsonBody[T]
            .examples(requestInput.examples.map { case (label, ex) =>
              Example(
                createInput(ex),
                Some(label),
                None
              )
            }.toList)
        )
    def inMapper(): Option[EndpointInput[_]] =
      inMapper(x => x)

    def inMapper[T <: Product: Encoder: Decoder: Schema](
        body: T
    ): Option[EndpointInput[_]] =
      inMapper(_ => body)

    lazy val noInputMapper: Option[EndpointInput[_]] =
      None

    def outMapper[
        T <: Product | CamundaVariable | Json | Map[String, CamundaVariable] |
          Seq[
            Product | CamundaVariable | Json | Map[String, CamundaVariable]
          ]: Encoder: Decoder: Schema
    ](
        createOutput: (example: Out) => T
    ): Option[EndpointOutput[_]] =
      if (requestOutput.noOutdput)
        None
      else
        Some(
          oneOf[T](
            oneOfMappingValueMatcher(
              requestOutput.statusCode,
              jsonBody[T]
                .examples(requestOutput.examples.map { case (name, ex: Out) =>
                  Example(
                    createOutput(ex),
                    Some(name),
                    None
                  )
                }.toList)
            ) { case _ =>
              true
            }
          )
        )
    def outMapper[
        T <: Product | Json | Map[String, CamundaVariable] |
          Seq[
            Product | Json | Map[String, CamundaVariable]
          ]: Encoder: Decoder: Schema
    ](
        body: T
    ): Option[EndpointOutput[_]] =
      outMapper(_ => body)

    def outMapper(): Option[EndpointOutput[_]] =
      outMapper(x => x)

    lazy val noOutputMapper: Option[EndpointOutput[_]] =
      None

  end CamundaRestApi

  object CamundaRestApi:

    def apply[
        In <: Product: Encoder: Decoder: Schema,
        Out <: Product: Encoder: Decoder: Schema
    ](
        e: InOutDescr[In, Out],
        tag: String,
        requestErrorOutputs: List[RequestErrorOutput]
    ): CamundaRestApi[In, Out] =
      CamundaRestApi(
        e.id,
        tag,
        e.descr,
        RequestInput(Map("standard" -> e.in)),
        RequestOutput.ok(Map("standard" -> e.out)),
        requestErrorOutputs
      )
  end CamundaRestApi

  case class ApiEndpoints(
      tag: String,
      endpoints: Seq[ApiEndpoint[_, _, _]]
  ):
    def create(): Seq[Endpoint[_, _, _, _]] =
      endpoints.flatMap(_.withTag(tag).create())

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]] =
      endpoints.flatMap(_.withTag(tag).createPostman())

  end ApiEndpoints

  sealed trait ApiEndpoint[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      T <: ApiEndpoint[In, Out, T]
  ] extends Product:
    def restApi: CamundaRestApi[In, Out]

    def apiName: String
    lazy val docName: String = s"${restApi.name}: ${apiName}"
    lazy val postmanName: String =
      s"${restApi.name}: ${getClass.getSimpleName}"
    lazy val valueName: String =
      docName.replace(": ", "")
    lazy val tag: String = restApi.tag
    def descr: String = restApi.maybeDescr.getOrElse("")
    lazy val inExample: In = restApi.requestInput.examples.values.head
    lazy val outExample: Out = restApi.requestOutput.examples.values.head
    def outStatusCode: StatusCode
    protected def inMapper(): Option[EndpointInput[_]] =
      restApi.inMapper()
    protected def outMapper(): Option[EndpointOutput[_]] =
      restApi.outMapper()
    protected def inMapperPostman(): Option[EndpointInput[_]] =
      restApi.inMapper()

    def withRestApi(restApi: CamundaRestApi[In, Out]): T

    def withName(n: String): T =
      withRestApi(restApi.copy(name = n))

    def withTag(t: String): T =
      withRestApi(restApi.copy(tag = t))

    def withDescr(description: String): T =
      withRestApi(restApi.copy(descr = Some(description)))

    def withInExample(label: String, example: In): T =
      withRestApi(
        restApi.copy(requestInput = restApi.requestInput :+ (label, example))
      )

    def withOutExample(label: String, example: Out): T =
      withRestApi(
        restApi.copy(requestOutput = restApi.requestOutput :+ (label, example))
      )

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]]

    def postmanBaseEndpoint: Endpoint[_, _, _, _] =
      Some(
        endpoint
          .name(postmanName)
          .tag(tag)
          .summary(postmanName)
          .description(descr)
      ).map(ep => inMapperPostman().map(ep.in).getOrElse(ep)).get

    def create(): Seq[Endpoint[_, _, _, _]] =
      Seq(
        endpoint
          .name(docName)
          .tag(tag)
          .in("api-docs" / valueName)
          .summary(docName)
          .description(descr)
          .head
      ).map(ep => inMapper().map(ep.in).getOrElse(ep))
        .map(ep => outMapper().map(ep.out).getOrElse(ep))

  end ApiEndpoint

  case class StartProcessInstance[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      processDefinitionKey: String,
      restApi: CamundaRestApi[In, Out]
  ) extends ApiEndpoint[In, Out, StartProcessInstance[In, Out]]:
    val apiName = "Process"
    val outStatusCode = StatusCode.Ok

    def withRestApi(
        restApi: CamundaRestApi[In, Out]
    ): StartProcessInstance[In, Out] =
      copy(restApi = restApi)

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]] =
      Seq(
        postmanBaseEndpoint
          .in(postPath(processDefinitionKey))
          .post
      )

    private def postPath(name: String)(implicit tenantId: Option[String]) =
      val basePath =
        "process-definition" / "key" / definitionKeyPath(name)
      tenantId
        .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
        .getOrElse(basePath / "start")

    override protected def inMapperPostman() =
      restApi.inMapper[StartProcessIn] { (example: In) =>
        StartProcessIn(
          CamundaVariable.toCamunda(example)
        )
      }

    override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
      s"""
         |
         |Usage as _CallActivity_:
         |```
         |lazy val $valueName =
         |          callActivity("$processDefinitionKey") //TODO adjust to your CallActivity id!
         |            .calledElement("$processDefinitionKey")
         |            ${inSources}
         |            ${outSources}
         |```
         |""".stripMargin

    private lazy val inSources =
      inExample match
        case NoInput() => ""
        case _ =>
          inExample.productElementNames.mkString(
            """.inSource("""",
            """")
              |            .inSource("""".stripMargin,
            """")"""
          )

    private lazy val outSources =
      outExample match
        case NoOutput() => ""
        case _ =>
          outExample.productElementNames.mkString(
            """.outSource("""",
            """")
              |            .outSource("""".stripMargin,
            """")"""
          )

  end StartProcessInstance

  object StartProcessInstance:

    def apply[
        In <: Product: Encoder: Decoder: Schema,
        Out <: Product: Encoder: Decoder: Schema
    ](e: InOutDescr[In, Out]): StartProcessInstance[In, Out] =
      StartProcessInstance[In, Out](
        e.id,
        CamundaRestApi(
          e,
          e.id,
          startProcessInstanceErrors
        )
      )

  end StartProcessInstance

  case class GetTaskFormVariables[
      Out <: Product: Encoder: Decoder: Schema
  ](
      restApi: CamundaRestApi[NoInput, Out]
  ) extends ApiEndpoint[NoInput, Out, GetTaskFormVariables[Out]]:

    val apiName = "no API!"

    val outStatusCode = StatusCode.Ok

    def withRestApi(
        restApi: CamundaRestApi[NoInput, Out]
    ): GetTaskFormVariables[Out] =
      copy(restApi = restApi)

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]] =
      Seq(
        postmanBaseEndpoint
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
      )

    private lazy val getPath =
      "task" / taskIdPath() / "form-variables" / s"--REMOVE:${restApi.name}--"

    override protected def inMapperPostman() =
      restApi.noInputMapper

  end GetTaskFormVariables

  case class CompleteTask[
      In <: Product: Encoder: Decoder: Schema
  ](
      restApi: CamundaRestApi[In, NoOutput]
  ) extends ApiEndpoint[In, NoOutput, CompleteTask[In]]:

    val outStatusCode = StatusCode.Ok
    val apiName = "no API!"

    def withRestApi(
        restApi: CamundaRestApi[In, NoOutput]
    ): CompleteTask[In] =
      copy(restApi = restApi)

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]] =
      Seq(
        postmanBaseEndpoint
          .in(postPath)
          .post
      )

    private lazy val postPath =
      "task" / taskIdPath() / "complete" / s"--REMOVE:${restApi.name}--"

    override protected def inMapperPostman() =
      restApi.inMapper[CompleteTaskIn] { (example: In) =>
        CompleteTaskIn(CamundaVariable.toCamunda(example))
      }

  end CompleteTask

  case class GetActiveTask(
      restApi: CamundaRestApi[NoInput, NoOutput]
  ) extends ApiEndpoint[NoInput, NoOutput, GetActiveTask]:

    val apiName = "no API!"
    val outStatusCode = StatusCode.Ok

    def withRestApi(
        restApi: CamundaRestApi[NoInput, NoOutput]
    ): GetActiveTask = copy(restApi = restApi)

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]] =
      Seq(
        postmanBaseEndpoint
          .in(postPath)
          .post
      )

    private lazy val postPath =
      "task" / s"--REMOVE:${restApi.name}--"

    override protected def inMapperPostman() =
      restApi.inMapper(GetActiveTaskIn())

  end GetActiveTask

  case class UserTaskEndpoint[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      restApi: CamundaRestApi[In, Out],
      getActiveTask: GetActiveTask,
      getTaskFormVariables: GetTaskFormVariables[In],
      completeTask: CompleteTask[Out]
  ) extends ApiEndpoint[In, Out, UserTaskEndpoint[In, Out]]:
    val outStatusCode = StatusCode.Ok //not used
    val apiName = "UserTask"

    def withRestApi(
        restApi: CamundaRestApi[In, Out]
    ): UserTaskEndpoint[In, Out] = copy(restApi = restApi)

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[_, _, _, _]] =
      val in = completeTask.restApi.copy(
        requestInput = RequestInput(restApi.requestOutput.examples)
      )
      val out = getTaskFormVariables.restApi.copy(requestOutput =
        RequestOutput(outStatusCode, restApi.requestInput.examples)
      )
      getActiveTask
        .withTag(restApi.tag)
        .createPostman() ++
        getTaskFormVariables
          .withRestApi(out)
          .withTag(restApi.tag)
          .createPostman() ++
        completeTask
          .withRestApi(in)
          .withTag(restApi.tag)
          .createPostman()

  end UserTaskEndpoint

  case class EvaluateDecision[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDmn: DecisionDmn[In, Out],
      restApi: CamundaRestApi[In, Out]
  ) extends ApiEndpoint[In, Out, EvaluateDecision[In, Out]]:

    val outStatusCode = StatusCode.Ok
    val apiName = "DecisionDmn"
    val decisionDefinitionKey = decisionDmn.decisionDefinitionKey

    def withRestApi(
        restApi: CamundaRestApi[In, Out]
    ): EvaluateDecision[In, Out] =
      copy(restApi = restApi)

    override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
      s"""
         |
         |Decision DMN:
         |- _decisionDefinitionKey_: `$decisionDefinitionKey`,
         |""".stripMargin

    def createPostman()(implicit
        tenantId: Option[String]
    ): Seq[Endpoint[?, ?, ?, ?]] =
      Seq(
        postmanBaseEndpoint
          .in(postPath(decisionDefinitionKey))
          .post
      )

    private def postPath(name: String)(implicit tenantId: Option[String]) =
      val basePath =
        "decision-definition" / "key" / definitionKeyPath(name)
      tenantId
        .map(id => basePath / "tenant-id" / tenantIdPath(id) / "evaluate")
        .getOrElse(basePath / "evaluate") / s"--REMOVE:${restApi.name}--"

    override protected def inMapperPostman() =
      restApi.inMapper[EvaluateDecisionIn] { (example: In) =>
        EvaluateDecisionIn(
          CamundaVariable.toCamunda(example)
        )
      }
  end EvaluateDecision

  private def definitionKeyPath(key: String) =
    path[String]("key")
      .description(
        "The Process- or Decision-DefinitionKey of the Process or Decision"
      )
      .default(key)

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

  lazy val startProcessInstanceErrors = List(
    badRequest(
      s"""The instance could not be created due to an invalid variable value,
         |for example if the value could not be parsed to an Integer value or the passed variable type is not supported.
         |$errorHandlingLink""".stripMargin
    ),
    notFound(
      s"""The instance could not be created due to a non existing process definition key.
         |$errorHandlingLink""".stripMargin
    ),
    serverError(s"""The instance could not be created successfully.
                   |$errorHandlingLink""".stripMargin)
  )

  lazy val evaluateDecisionErrors = List(
    forbidden(
      s"""The authenticated user is unauthorized to evaluate this decision.
         |$errorHandlingLink""".stripMargin
    ),
    notFound(
      s"""The decision could not be evaluated due to a nonexistent decision definition.
         |$errorHandlingLink""".stripMargin
    ),
    serverError(s"""The decision could not be evaluated successfully,
                   | e.g. some of the input values are not provided but they are required.
                   |$errorHandlingLink""".stripMargin)
  )

  lazy val getActiveTaskErrors = List(
    badRequest(
      s"""Returned if some of the query parameters are invalid, for example if a sortOrder parameter is supplied, but no sortBy,
         | or if an invalid operator for variable comparison is used.
         |$errorHandlingLink""".stripMargin
    )
  )

  lazy val getTaskFormVariablesErrors = List(
    notFound(s"""Task id is null or does not exist.
                |$errorHandlingLink""".stripMargin)
  )

  lazy val completeTaskErrors = List(
    badRequest(
      s"""The variable value or type is invalid, for example if the value could not be parsed to an Integer value or the passed variable type is not supported.
         |$errorHandlingLink""".stripMargin
    ),
    serverError(
      s"""If the task does not exist or the corresponding process instance could not be resumed successfully.
         |$errorHandlingLink""".stripMargin
    )
  )

  def badRequest(msg: String = "Bad Request"): RequestErrorOutput =
    error(StatusCode.BadRequest).example(
      CamundaError("BadRequest", msg)
    )

  def notFound(msg: String = "Not Found"): RequestErrorOutput =
    error(StatusCode.NotFound).example(
      CamundaError("NotFound", msg)
    )

  def forbidden(msg: String = "Forbidden"): RequestErrorOutput =
    error(StatusCode.Forbidden).example(
      CamundaError("Forbidden", msg)
    )

  def serverError(msg: String = "Internal Server Error"): RequestErrorOutput =
    error(StatusCode.InternalServerError).example(
      CamundaError("InternalServerError", msg)
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
    s"See the [Introduction](https://docs.camunda.org/manual/$camundaVersion/reference/rest/overview/#error-handling) for the error response format."

end endpoints
case class MyStr(str: String) extends AnyVal
