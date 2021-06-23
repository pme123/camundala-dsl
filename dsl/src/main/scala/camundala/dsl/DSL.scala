package camundala
package dsl
import model.*

trait DSL
    extends bpmns
    with processes
    with groups
    with users
    with events
    with flows
    with forms
    with forms.constraints
    with tasks
    with callActivities
    with props
    with taskImplementations
    with parameters
    with variables
    with transactions:

  def ident(id: String): Ident =
    Ident(id)

  def path(pathStr: String): BpmnPath =
    BpmnPath(pathStr)

  def name(name: String): Name =
    Name(name)

  def tenantId(ti: String): TenantId =
    TenantId(ti)

object DSL extends DSL:

  trait Givens:

    given Conversion[Ident, String] = _.toString

    given Conversion[BpmnPath, String] = _.toString

    given Conversion[Name, String] = _.toString

    given Conversion[String, Ident] = ident(_)

    given Conversion[String, BpmnPath] = path(_)

    given Conversion[String, Name] = name(_)

  object Givens extends Givens
