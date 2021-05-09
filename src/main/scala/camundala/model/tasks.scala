package camundala.model

import camundala.model.BpmnProcess.ElemKey
import camundala.model.GeneratedForm.FormField
import camundala.model.TaskImplementation._

case class Activity(
    processNode: ProcessNode,
    inputParameters: Seq[InOutParameter] = Seq.empty
):
  val ident = processNode.ident
  val properties: Properties = processNode.properties

  def inputs(params: InOutParameter*): Activity = copy(inputParameters = params)

  def prop(prop: Property): Activity = copy(processNode = processNode.prop(prop))

  def asyncBefore: Activity = copy(processNode = processNode.asyncBefore)

  def asyncAfter: Activity = copy(processNode = processNode.asyncAfter)

case class Task(activity: Activity) extends HasIdent:
  val ident = activity.ident

  val properties: Properties = activity.properties

  def prop(prop: Property): Task = copy(activity = activity.prop(prop))

object Activity:
  def apply(ident: String): Activity =
    Activity(ProcessNode(ident))

object Task:

  def apply(ident: String): Task =
    Task(Activity(ident))

sealed trait TaskImplementation

object TaskImplementation:

  case class Expression(
      private val expression: String,
      resultVariable: Option[String] = None
  ) extends TaskImplementation
      with BusinessRuleTaskImpl

  object Expression:
    def apply(expr: String): Expression =
      new Expression(
        if (expr.startsWith("$"))
          expr
        else
          s"$${$expr}"
      )
  case class DelegateExpression(expresssion: String)
      extends TaskImplementation
      with BusinessRuleTaskImpl

  case class JavaClass(className: String)
      extends TaskImplementation
      with BusinessRuleTaskImpl

  case class ExternalTask(topic: String) extends TaskImplementation

sealed trait BusinessRuleTaskImpl

object BusinessRuleTaskImpl:

  opaque type DecisionRef = String

  object DecisionRef:
    def apply(ref: String): DecisionRef = ref

  case class Dmn(
      decisionRef: DecisionRef,
      binding: RefBinding = RefBinding.Latest,
      resultVariable: Option[ResultVariable] = None,
      tenantId: Option[TenantId] = None
  ) extends BusinessRuleTaskImpl

case class ResultVariable(name: Name, mapDecisionResult: MapDecisionResult)

enum MapDecisionResult(val label: String):

  // TypedValue
  case SingleEntry extends MapDecisionResult("singleEntry")

  // Map[String, Object]
  case SingleResult extends MapDecisionResult("singleResult")

  // List[Object]
  case CollectEntries extends MapDecisionResult("collectEntries")

  // List[Map[String, Object]]
  case ResultList extends MapDecisionResult("resultList")

sealed trait RefBinding:
  def binding: String

object RefBinding:

  case object Latest extends RefBinding:
    val binding: String = "latest"

  case object Deployment extends RefBinding:
    val binding: String = "latest"

  case class Version(version: String) extends RefBinding:
    val binding: String = s"""version("$version")"""

  case class VersionTag(tag: String) extends RefBinding:
    val binding: String = s"""versionTag("$tag")"""

case class ServiceTask(
    task: Task,
    taskImplementation: TaskImplementation
) extends HasTask[ServiceTask]
    with HasTaskImplementation[ServiceTask]:
  val elemType: ElemKey = ElemKey.serviceTasks

  def withTask(task: Task): ServiceTask = copy(task = task)

  def taskImplementation(taskImplementation: TaskImplementation): ServiceTask =
    copy(taskImplementation = taskImplementation)

object ServiceTask:

  def apply(ident: String): ServiceTask =
    ServiceTask(Task(ident), ExternalTask("my-topic"))

case class SendTask(
    task: Task,
    taskImplementation: TaskImplementation = Expression("")
) extends HasTask[SendTask]
    with HasTaskImplementation[SendTask]:
  val elemType: ElemKey = ElemKey.sendTasks

  def withTask(task: Task): SendTask = copy(task = task)

  def taskImplementation(taskImplementation: TaskImplementation): SendTask =
    copy(taskImplementation = taskImplementation)

case class BusinessRuleTask(
    task: Task,
    taskImplementation: BusinessRuleTaskImpl = Expression("")
) extends HasTask[BusinessRuleTask] :
    //  with HasTaskImplementation[BusinessRuleTask] // TODO DMN Table

  val elemKey: ElemKey = ElemKey.businessRuleTasks

  def withTask(task: Task): BusinessRuleTask = copy(task = task)

  def taskImplementation(
      taskImplementation: TaskImplementation
  ): BusinessRuleTask =
    this //TODO copy(taskImplementation = taskImplementation)

case class UserTask(task: Task, bpmnForm: Option[BpmnForm] = None)
    extends HasTask[UserTask]
    with HasForm[UserTask]:

  val elemType = ElemKey.userTasks

  def withTask(task: Task): UserTask = copy(task = task)

  def form(form: BpmnForm): UserTask = copy(bpmnForm = Some(form))

object UserTask:

  def apply(ident: String): UserTask =
    UserTask(Task(ident))
