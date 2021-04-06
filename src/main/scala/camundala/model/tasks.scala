package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.GeneratedForm.FormField
import camundala.model.TaskImplementation._

case class Activity(ident: Ident)
  extends HasIdent
    with HasStringify :

  def stringify(intent: Int): String = ident.stringify(intent)

case class Task(activity: Activity)
  extends HasActivity
    with HasStringify :

  def stringify(intent: Int): String = activity.stringify(intent)

object Task {

  def apply(ident: Ident): Task =
    Task(Activity(ident))

}

sealed trait TaskImplementation
  extends HasStringify

object TaskImplementation:

  case class Expression(private val expression: String, resultVariable: Option[String] = None)
    extends TaskImplementation 
    with BusinessRuleTaskImpl:

    def stringify(intent: Int): String = s"""${intentStr(intent)}.expression("$expression"${resultVariable.map(v => s""", "$v"""").getOrElse("")})"""

  object Expression :
    def apply(expr: String):Expression =
      new Expression(
        if(expr.startsWith("$"))
          expr
        else
          s"$${$expr}"  
      )
  case class DelegateExpression(expresssion: String)
    extends TaskImplementation
    with BusinessRuleTaskImpl :
    def stringify(intent: Int): String = s"""${intentStr(intent)}.delegateExpression("$expresssion")"""

  case class JavaClass(className: String)
    extends TaskImplementation 
    with BusinessRuleTaskImpl:
    def stringify(intent: Int): String = s"""${intentStr(intent)}.javaClass("$className")"""

  case class ExternalTask(topic: String)
    extends TaskImplementation 
    with BusinessRuleTaskImpl:
    def stringify(intent: Int): String = s"""${intentStr(intent)}.externalTask("$topic")"""

sealed trait BusinessRuleTaskImpl

object BusinessRuleTask:

  opaque type DecisionRef = String

  object DecisionRef:
    def apply(ref: String): DecisionRef = ref

  extension (ref: DecisionRef)
    def stringify(intent: Int): String = s"""${intentStr(intent)}.decisionRef("$ref")"""


  case class Dmn(decisionRef: DecisionRef,
                 binding: RefBinding = RefBinding.Latest,
                 resultVariable: Option[ResultVariable] = None,
                 tenantId: Option[TenantId] = None)
    extends BusinessRuleTaskImpl :
      
    def stringify(intent: Int): String =
      s"""${intentStr(intent)}dmn(
         |${decisionRef.stringify(intent + 1)},
         |${
        (Seq(binding.stringify(intent + 1)) ++
          resultVariable.map(rv => rv.stringify(intent + 1)).toSeq ++
          tenantId.map(ti => ti.stringify(intent + 1)).toSeq).mkString(",\n")
      }
         |${intentStr(intent)})""".stripMargin

case class ResultVariable(name: Name, mapDecisionResult: MapDecisionResult):
  def stringify(intent: Int): String =
    s"""${intentStr(intent)}resultVariable(
       |${name.stringify(intent + 1)},
       |${mapDecisionResult.stringify(intent + 1)})""".stripMargin

enum MapDecisionResult(val label: String):
  def stringify(intent: Int): String = s"${intentStr(intent)}$label"

  // TypedValue
  case SingleEntry extends MapDecisionResult("singleEntry")

  // Map[String, Object]
  case SingleResult extends MapDecisionResult("singleResult")

  // List[Object]
  case CollectEntries extends MapDecisionResult("collectEntries")

  // List[Map[String, Object]]
  case ResultList extends MapDecisionResult("resultList")

sealed trait RefBinding
  extends HasStringify :
  def binding: String

  def stringify(intent: Int): String = s"${intentStr(intent)}binding($binding)"


object RefBinding:

  case object Latest
    extends RefBinding :
    val binding: String = "latest"

  case object Deployment
    extends RefBinding :
    val binding: String = "latest"


  case class Version(version: String)
    extends RefBinding :
    val binding: String = s"""version("$version")"""

  case class VersionTag(tag: String)
    extends RefBinding :
    val binding: String = s"""versionTag("$tag")"""

case class ServiceTask(task: Task,
                       taskImplementation: TaskImplementation)
  extends HasTask
    with HasTaskImplementation[ServiceTask]
    with ProcessElement :
  val elemType: NodeKey = NodeKey.serviceTasks
  def taskImplementation(taskImplementation: TaskImplementation): ServiceTask = copy(taskImplementation = taskImplementation)

object ServiceTask:

  def apply(ident: Ident): ServiceTask =
    ServiceTask(Task(ident), ExternalTask("my-topic"))

case class SendTask(task: Task,
                    taskImplementation: TaskImplementation = Expression(""))
  extends HasTask
    with HasTaskImplementation[SendTask]
    with ProcessElement :
  val elemType: NodeKey = NodeKey.sendTasks
  def taskImplementation(taskImplementation: TaskImplementation): SendTask = copy(taskImplementation = taskImplementation)

case class BusinessRuleTask(task: Task,
                            taskImplementation: BusinessRuleTaskImpl = Expression(""))
  extends HasTask
  //  with HasTaskImplementation[BusinessRuleTask] // TODO DMN Table
    with ProcessElement :
  val elemType: NodeKey = NodeKey.businessRuleTasks
  def taskImplementation(taskImplementation: TaskImplementation): BusinessRuleTask = this //TODO copy(taskImplementation = taskImplementation)
  def stringify(intent: Int): String = "----BusinessRuleTask"

case class UserTask(task: Task,
                    bpmnForm: Option[BpmnForm] = None)
  extends HasTask
    with HasForm[UserTask]
    with ProcessElement :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}userTask(${task.ident.stringify(0)})${
        bpmnForm.map(_.stringify(intent + 1)).toSeq.mkString(",\n")}""".stripMargin

  val elemType = NodeKey.userTasks

  def form(form: BpmnForm): UserTask = copy(bpmnForm = Some(form))

object UserTask:

  def apply(ident: Ident): UserTask =
    UserTask(Task(ident))
