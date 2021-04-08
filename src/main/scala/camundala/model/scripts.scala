package camundala.model

import camundala.model.*
import camundala.model.BpmnProcess.NodeKey
import camundala.model.ScriptImplementation.{ExternalScript, InlineScript, ScriptPath}
import camundala.model.TaskImplementation.Expression

case class ScriptTask(task: Task,
                      scriptImplementation: ScriptImplementation = InlineScript(ScriptLanguage.Groovy, ""),
                      resultVariable: Option[Ident] = None,
                      isAsyncBefore: Boolean = false,
                      isAsyncAfter: Boolean = false
                     )
  extends HasTask
    with ProcessNode :
  val elemType = NodeKey.userTasks

  def asyncBefore: ScriptTask = copy(isAsyncBefore = true)

  def asyncAfter: ScriptTask = copy(isAsyncAfter = true)

  def stringify(intent: Int): String =
    stringifyElements(intent, s"scriptTask(${task.ident.stringify(0)})",
      Seq(scriptImplementation.stringify(intent + 1)) ++
        resultVariable.map(v => s"resultVariable(${v.stringify()})").toSeq: _*)

sealed trait ScriptImplementation:
  def language: ScriptLanguage

  def stringify(intent: Int): String

object ScriptImplementation:

  type ScriptPath = String

  case class InlineScript(language: ScriptLanguage,
                          script: String,
                         ) extends ScriptImplementation :
    def stringify(intent: Int): String = language match {
      case ScriptLanguage.Groovy =>
        s"""inlineGroovy(\"\"\"$script\"\"\")"""
      case ScriptLanguage.Javascript =>
        s"""inlineJavascript(\"\"\"$script\"\"\")"""
    }
      

  case class ExternalScript(language: ScriptLanguage,
                            resource: ScriptPath,
                           )extends ScriptImplementation :
    val deployResource = s"deployment://$resource"

    def stringify(intent: Int): String = language match {
      case ScriptLanguage.Groovy =>
        s"""groovyRef("$resource")"""
      case ScriptLanguage.Javascript =>
        s"""javascriptRef("$resource")"""
    }

// Extension methods define opaque types' public APIs
enum ScriptLanguage(val filePostfix: String):

  case Groovy extends ScriptLanguage(".groovy")

  case Javascript extends ScriptLanguage(".js")


