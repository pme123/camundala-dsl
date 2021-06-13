package camundala
package dsl

import BusinessRuleTaskImpl.*
import ScriptImplementation.*
import TaskImplementation.*
import model.{Ident, TenantId}

trait tasks:

  extension[T](hasTaskImpl: HasTaskImplementation[T])
    def expression(expr: String) =
      hasTaskImpl.taskImplementation(Expression(expr))

    def expression(expr: String, resultVariable: String) =
      hasTaskImpl.taskImplementation(Expression(expr, Some(resultVariable)))

    def delegateExpression(expr: String) =
      hasTaskImpl.taskImplementation(DelegateExpression(expr))

    def javaClass(className: String) =
      hasTaskImpl.taskImplementation(JavaClass(className))

    def externalTask(topic: String) =
      hasTaskImpl.taskImplementation(ExternalTask(topic: String))

  def serviceTask(ident: String) =
    ServiceTask(ident)

  def sendTask(ident: String) =
    SendTask(Task(ident))

  def scriptTask(ident: String) =
    //   scriptImplementation: ScriptImplementation,
    // resultVariable: Option[Ident] = None) =
    ScriptTask(Task(ident))

  extension (sTask: ScriptTask)

    def groovyRef(scriptPath: ScriptPath): ScriptTask =
      sTask.copy(scriptImplementation =
        ExternalScript(ScriptLanguage.Groovy, s"$scriptPath.groovy")
      )

    def inlineGroovy(script: String): ScriptTask =
      sTask.copy(scriptImplementation =
        InlineScript(ScriptLanguage.Groovy, script)
      )

    def javascriptRef(scriptPath: ScriptPath): ScriptTask =
      sTask.copy(scriptImplementation =
        ExternalScript(ScriptLanguage.Javascript, s"$scriptPath.groovy")
      )

    def inlineJavascript(script: String): ScriptTask =
      sTask.copy(scriptImplementation =
        InlineScript(ScriptLanguage.Javascript, script)
      )

    def resultVariable(resultVariable: String): ScriptTask =
      sTask.copy(resultVariable = Some(Ident(resultVariable)))

  def businessRuleTask(ident: String) =
    BusinessRuleTask(Task(ident))

  extension (brTask: BusinessRuleTask)
    def impl(d: Dmn) = brTask.copy(taskImplementation = d)

  def userTask(ident: String) =
    UserTask(ident)

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

  def dmn(decisionRef: String) =
    Dmn(DecisionRef(decisionRef))

  extension (dmn: Dmn)
    def binding(refBinding: RefBinding): Dmn =
      dmn.copy(binding = refBinding)

    def latest: Dmn =
      dmn.copy(binding = RefBinding.Latest)

    def deployment: Dmn =
      dmn.copy(binding = RefBinding.Deployment)

    def tenantId(id: String): Dmn =
      dmn.copy(tenantId = Some(TenantId(id)))

    def version(v: String): Dmn = dmn.copy(binding = RefBinding.Version(v))

    def versionTag(tag: String): Dmn =
      dmn.copy(binding = RefBinding.VersionTag(tag))

    def resultVariable(name: String, mapDecisionResult: MapDecisionResult) =
      dmn.copy(resultVariable =
        Some(ResultVariable(Name(name), mapDecisionResult))
      )

    def singleEntry(name: String): Dmn =
      resultVariable(name, MapDecisionResult.SingleEntry)

    def singleResult(name: String): Dmn =
      resultVariable(name, MapDecisionResult.SingleResult)

    def collectEntries(name: String): Dmn =
      resultVariable(name, MapDecisionResult.CollectEntries)

    def resultList(name: String): Dmn =
      resultVariable(name, MapDecisionResult.ResultList)
