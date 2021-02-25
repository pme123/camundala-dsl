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

trait HasTaskImplementation[T]:
  def taskImplementation: TaskImplementation

  def implementation(impl: TaskImplementation): T

  def delegate(expression: String): T =
    implementation(DelegateExpression(expression))

  def expression(expr: String): T =
    implementation(Expression(expr))

  def javaClass(className: String): T =
    implementation(JavaClass(className))

  def external(topic: String): T =
    implementation(ExternalTask(topic))

trait HasForm[T]:
  def bpmnForm: Option[BpmnForm]

case class Activity(ident: Ident)
  extends HasIdent

case class Task(activity: Activity)
  extends HasActivity
    with HasStringify:

object Task {

  def apply(ident: Ident): Task =
    Task(Activity(ident))

}

sealed trait TaskImplementation

object TaskImplementation:

  case class Expression(private val expression: String, resultVariable: Option[String] = None)
    extends TaskImplementation {

    def resultVariable(resultVariable: String): Expression =
      copy(resultVariable = Some(resultVariable))
  }

  object Expression {
    def apply(expr: String): Expression =
      new Expression(s"$${$expr}")
  }

  case class DelegateExpression(expresssion: String)
    extends TaskImplementation

  case class JavaClass(className: String)
    extends TaskImplementation

  case class ExternalTask(topic: String)
    extends TaskImplementation

case class ServiceTask(task: Task,
                       taskImplementation: TaskImplementation)
  extends HasTask
    with HasTaskImplementation[ServiceTask]
    with ProcessElement :
  val elemType: NodeKey = NodeKey.serviceTasks

  def stringify(intent: Int): String = "serviceTask----"

  def implementation(impl: TaskImplementation) = copy(taskImplementation = impl)

object ServiceTask:

  def apply(ident: Ident): ServiceTask =
    ServiceTask(Task(ident), ExternalTask("my-topic"))

case class UserTask(task: Task,
                    bpmnForm: Option[BpmnForm] = None)
  extends HasTask
    with HasForm[UserTask]
    with ProcessElement :

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}userTask(
       |${
      task.stringify(intent + 1)
      (Seq(ident.stringify(intent + 1)) ++
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
