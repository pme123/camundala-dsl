package camundala.dsl

import camundala.model.BusinessRuleTask.{DecisionRef, Dmn}
import camundala.model.TaskImplementation.{DelegateExpression, Expression, ExternalTask, JavaClass}
import camundala.model._

trait tasks:
  def serviceTask(ident: Ident,
                  taskImplementation: TaskImplementation) =
    ServiceTask(Task(ident), taskImplementation)

  def sendTask(ident: Ident,
               taskImplementation: TaskImplementation) =
    SendTask(Task(ident), taskImplementation)

  def businessRuleTask(ident: Ident,
                       taskImplementation: BusinessRuleTaskImpl) =
    BusinessRuleTask(Task(ident), taskImplementation)


trait taskImplementations:

  def expression(expr: String) =
    Expression(expr)

  def expression(expr: String, resultVariable: String) =
    Expression(expr, Some(resultVariable))

  def delegateExpression(expr: String) =
    DelegateExpression(expr)

  def javaClass(className: String) =
    JavaClass(className)

  def externalTask(topic: String) =
    ExternalTask(topic: String)

  type DmnAttr = RefBinding | ResultVariable | TenantId

  def dmn(decisionRef: DecisionRef,
          attrs: DmnAttr*) =
    Dmn(decisionRef,
      binding = attrs.collect { case rb: RefBinding => rb }.headOption.getOrElse(latest),
      resultVariable = attrs.collect { case rv: ResultVariable => rv }.headOption,
      tenantId = attrs.collect { case ti: String => TenantId(ti) }.headOption
    )

  def decisionRef(ref: String) =
    DecisionRef(ref)

  def binding(refBinding: RefBinding) =
    refBinding

  def latest: RefBinding = RefBinding.Latest

  def deployment: RefBinding = RefBinding.Deployment

  def version(v: String): RefBinding = RefBinding.Version(v)

  def versionTag(tag: String): RefBinding = RefBinding.VersionTag(tag)

  def resultVariable(name: Name, mapDecisionResult: MapDecisionResult = resultList) =
    ResultVariable(name, mapDecisionResult)

  def singleEntry = MapDecisionResult.SingleEntry

  def singleResult = MapDecisionResult.SingleResult

  def collectEntries = MapDecisionResult.CollectEntries

  def resultList = MapDecisionResult.ResultList

trait userTasks:
  def userTask(ident: Ident) =
    UserTask(ident)

  def userTask(ident: Ident, form: BpmnForm) =
    UserTask(Task(ident), Some(form))
