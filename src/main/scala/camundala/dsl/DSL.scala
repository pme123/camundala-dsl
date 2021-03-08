package camundala.dsl

import camundala.model.BpmnGroup.GroupType
import camundala.model.BpmnUser._
import camundala.model.Constraint._
import camundala.model.GeneratedForm.FormField
import camundala.model.GeneratedForm.FormFieldType._
import camundala.model.TaskImplementation._
import camundala.model.{CandidateGroups, CandidateUsers, ProcessElement, _}
import camundala.dsl._

import scala.language.implicitConversions

trait DSL
  extends bpmns
    with processes
    with groups
    with users
    with events
    with forms
    with forms.constraints
    with props
    with tasks
    with taskImplementations
    with userTasks :

  def ident(id: String): Ident =
    Ident(id)

  def path(pathStr: String): BpmnPath =
    BpmnPath(pathStr)

  def name(name: String): Name =
    Name(name)

  def tenantId(ti: String): TenantId =
    TenantId(ti)

object DSL extends DSL :

  trait Implicits:

    given Conversion[Ident, String] = _.toString

    given Conversion[BpmnPath, String] = _.toString

    given Conversion[Name, String] = _.toString

    given Conversion[String, Ident] = ident(_)

    given Conversion[String, BpmnPath] = path(_)

    given Conversion[String, Name] = name(_)

  object Implicits extends Implicits


