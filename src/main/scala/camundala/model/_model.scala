package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.GeneratedForm.FormField
import camundala.model.TaskImplementation._

//type IdRegex = MatchesRegex["""^[a-zA-Z_][\w\-\.]+$"""]
//type EmailRegex = MatchesRegex["""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""]

def intentStr(intent: Int) = "  " * intent

def stringifyWrap(intent: Int, name: String, body: HasStringify): String =
  s"""${intentStr(intent)}$name(
     |${body.stringify(intent + 1)}
     |${intentStr(intent)})""".stripMargin

def stringifyWrap(intent: Int, name: String, entries: Seq[_ <: HasStringify]): String =
  s"""${intentStr(intent)}$name(
     |${entries.map(_.stringify(intent + 1)).mkString(",\n")}
     |${intentStr(intent)})""".stripMargin

def stringifyElements(intent: Int, name: String, elements: String*): String =
  s"""${intentStr(intent)}$name(
     |${elements.map(e => intentStr(intent + 1) + e).mkString(",\n")}
     |${intentStr(intent)})""".stripMargin

trait HasStringify:
  def stringify(intent: Int): String

def stringify(intent: Int, name: String)(body: Seq[HasStringify]) =
  s"""${intentStr(intent)}$name(
     |${body.map(_.stringify(intent + 1)).mkString(",\n")}
     |${intentStr(intent)})""".stripMargin

opaque type BpmnPath = String

object BpmnPath:
  def apply(path: String): BpmnPath = path

  extension (path: BpmnPath)
    def stringify(intent: Int): String = s"""${intentStr(intent)}path("$path")"""

opaque type Ident = String

object Ident:
  def apply(ident: String): Ident = ident

  extension (ident: Ident)
    def stringify(intent: Int): String = s"""${intentStr(intent)}ident("$ident")"""

opaque type Name = String

object Name:
  def apply(name: String): Name = name

  extension (name: Name)
    def stringify(intent: Int): String = s"""${intentStr(intent)}name("$name")"""

opaque type TenantId = String

object TenantId:
  def apply(tenantId: String): TenantId = tenantId

  extension (tenantId: TenantId)
    def stringify(intent: Int): String = s"""${intentStr(intent)}tenantId("$tenantId")"""

trait HasIdent:
  def ident: Ident

trait HasActivity
  extends HasIdent :
  def activity: Activity

  lazy val ident = activity.ident

trait HasTask
  extends HasActivity :
  def task: Task

  lazy val activity = task.activity

trait HasTaskImplementation:
  def elemType: NodeKey

  def task: Task

  def taskImplementation: TaskImplementation

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}${elemType.name}(
       |${task.stringify(intent + 1)},
       |${taskImplementation.stringify(intent + 1)}
       |${intentStr(intent)})""".stripMargin

trait HasForm[T]:
  def bpmnForm: Option[BpmnForm]

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

  case class Expression(expression: String, resultVariable: Option[String] = None)
    extends TaskImplementation :

    def stringify(intent: Int): String = s"""${intentStr(intent)}expression("$expression"${resultVariable.map(v => s""", "$v"""").getOrElse("")})"""

  case class DelegateExpression(expresssion: String)
    extends TaskImplementation :
    def stringify(intent: Int): String = s"""${intentStr(intent)}delegateExpression("$expresssion")"""

  case class JavaClass(className: String)
    extends TaskImplementation :
    def stringify(intent: Int): String = s"""${intentStr(intent)}javaClass("$className")"""

  case class ExternalTask(topic: String)
    extends TaskImplementation :
    def stringify(intent: Int): String = s"""${intentStr(intent)}externalTask("$topic")"""

sealed trait BusinessRuleTaskImpl
  extends TaskImplementation

object BusinessRuleTask:

  opaque type DecisionRef = String

  object DecisionRef:
    def apply(ref: String): DecisionRef = ref

  extension (ref: DecisionRef)
    def stringify(intent: Int): String = s"""${intentStr(intent)}decisionRef("$ref")"""


  case class Dmn(decisionRef: DecisionRef,
                 binding: RefBinding,
                 resultVariable: Option[ResultVariable],
                 tenantId: Option[TenantId])
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
    with HasTaskImplementation
    with ProcessElement :
  val elemType: NodeKey = NodeKey.serviceTasks

object ServiceTask:

  def apply(ident: Ident): ServiceTask =
    ServiceTask(Task(ident), ExternalTask("my-topic"))

case class SendTask(task: Task,
                    taskImplementation: TaskImplementation)
  extends HasTask
    with HasTaskImplementation
    with ProcessElement :
  val elemType: NodeKey = NodeKey.sendTasks

case class BusinessRuleTask(task: Task,
                            taskImplementation: BusinessRuleTaskImpl)
  extends HasTask
    with HasTaskImplementation
    with ProcessElement :
  val elemType: NodeKey = NodeKey.businessRuleTasks

case class UserTask(task: Task,
                    bpmnForm: Option[BpmnForm] = None)
  extends HasTask
    with HasForm[UserTask]
    with ProcessElement :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}userTask(
       |${
      (Seq(task.stringify(intent + 1)) ++
        bpmnForm.map(_.stringify(intent + 1)).toSeq).mkString(",\n")
    }
       |${intentStr(intent)})""".stripMargin

  val elemType = NodeKey.userTasks

  def form(form: BpmnForm): UserTask = copy(bpmnForm = Some(form))

object UserTask:

  def apply(ident: Ident): UserTask =
    UserTask(Task(ident))

opaque type ProcessVarString = Ident

object ProcessVarString:
  def apply(variable: String): ProcessVarString = variable

// Extension methods define opaque types' public APIs
extension (p: ProcessVarString)
// def ident: Ident = p

  def expression: Expression = Expression(p)
