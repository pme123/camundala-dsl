package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.GeneratedForm.FormField

//type IdRegex = MatchesRegex["""^[a-zA-Z_][\w\-\.]+$"""]
//type EmailRegex = MatchesRegex["""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""]

def intentStr(intent: Int) = "  " * intent

def stringifyWrap(intent: Int, name: String, body: HasStringify): String =
  s"""${intentStr(intent)}$name(
     |${body.stringify(intent + 1)}
     |${intentStr(intent)})""".stripMargin

def stringifyWrapFluent(intent: Int, name: String, entries: Seq[_ <: HasStringify]): String =
  s"""${intentStr(intent)}$name(
     |${entries.map(_.stringify(intent + 1)).mkString(s"\n${intentStr(intent+ 1)}.")}
     |${intentStr(intent)})""".stripMargin
     
def stringifyWrap(intent: Int, name: String, entries: Seq[_ <: HasStringify]): String =
  s"""${intentStr(intent)}$name(
     |${entries.map(_.stringify(intent + 1)).mkString(",\n")}
     |${intentStr(intent)})""".stripMargin

def stringifyElements(intent: Int, name: String, elements: String*): String =
  s"""${intentStr(intent)}$name
     |${intentStr(intent + 1)}.${elements.map(e =>  e).mkString(s"\n${intentStr(intent + 1)}.")}""".stripMargin

trait HasStringify:
  def stringify(intent: Int): String

opaque type BpmnPath = String

object BpmnPath:
  def apply(path: String): BpmnPath = path
  
  extension (path: BpmnPath)
    def stringify(intent: Int = 0): String = s"""${intentStr(intent)}"$path""""

opaque type Ident = String

object Ident:
  def apply(ident: String): Ident = ident

  extension (ident: Ident)
    def stringify(intent: Int = 0): String = s"""${intentStr(intent)}"$ident""""

opaque type Name = String

object Name:
  def apply(name: String): Name = name

  extension (name: Name)
    def stringify(intent: Int = 0): String = s"""${intentStr(intent)}"$name""""

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

trait HasTaskImplementation[T]:
  def elemType: NodeKey

  def task: Task

  def taskImplementation: TaskImplementation

  def taskImplementation(taskImplementation: TaskImplementation): T

  def stringify(intent: Int): String =
    s"""${intentStr(intent)}${elemType.name}(${task.ident.stringify(0)})
       |${taskImplementation.stringify(intent + 1)}""".stripMargin

trait HasForm[T]:
  def bpmnForm: Option[BpmnForm]
  def form(form:BpmnForm): T

trait HasProperties[T]:
  def properties: Properties
  def prop(prop: Property): T

opaque type ProcessVarString = Ident

object ProcessVarString:
  def apply(variable: String): ProcessVarString = variable

