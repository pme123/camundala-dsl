package camundala
package dsl
import model.*

trait DSL
    extends bpmns,
      dmns,
      processes,
      groups,
      users,
      events,
      flows,
      forms,
      forms.constraints,
      tasks,
      callActivities,
      props,
      taskImplementations,
      parameters,
      variables,
      listeners,
      transactions,
      inOutObjects:

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
