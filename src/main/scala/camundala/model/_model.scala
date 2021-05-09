package camundala.model

import camundala.model.BpmnProcess.ElemKey
import camundala.model.GeneratedForm.FormField

//type IdRegex = MatchesRegex["""^[a-zA-Z_][\w\-\.]+$"""]
//type EmailRegex = MatchesRegex["""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""]

opaque type BpmnPath = String

object BpmnPath:
  def apply(path: String): BpmnPath = path

opaque type Ident = String

object Ident:
  def apply(ident: String): Ident = ident

opaque type Name = String

object Name:
  def apply(name: String): Name = name

opaque type TenantId = String

object TenantId:
  def apply(tenantId: String): TenantId = tenantId

trait HasIdent:
  def ident: Ident

trait HasActivity[T <: HasActivity[T]]
    extends HasProcessNode[T]
    with HasInputParameters[T]:

  def activity: Activity

  def inputParameters = activity.inputParameters

trait HasTask[T <: HasTask[T]] extends HasActivity[T]:
  def task: Task

  def withTask(task: Task): T

  lazy val activity = task.activity

  def inputs(params: InOutParameter*): T = withTask(task.copy(activity = activity.inputs(params:_*)))

trait HasTaskImplementation[T]:
  def elemType: ElemKey

  def taskImplementation: TaskImplementation

  def taskImplementation(taskImplementation: TaskImplementation): T

trait HasForm[T]:
  def bpmnForm: Option[BpmnForm]

  def form(form: BpmnForm): T

trait HasProperties[T]:
  def properties: Properties

  def prop(prop: Property): T

trait HasTransactionBoundary[T <: HasTransactionBoundary[T]]:
  def isAsyncBefore: Boolean

  def isAsyncAfter: Boolean

  def asyncBefore: T

  def asyncAfter: T

trait HasInputParameters[T]:
  def inputParameters: Seq[InOutParameter]

  def inputs(params: Seq[InOutParameter]): T

opaque type ProcessVarString = Ident

object ProcessVarString:
  def apply(variable: String): ProcessVarString = variable

