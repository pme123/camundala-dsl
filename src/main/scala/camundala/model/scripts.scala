package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.ScriptImplementation.{ExternalScript, InlineScript, ScriptPath}
import camundala.model.TaskImplementation.Expression

case class ScriptTask(task: Task,
                      scriptImplementation: Option[ScriptImplementation] = None,
                      resultVariable: Option[String] = None)
  extends HasTask
    with ProcessElement :
  val elemType = NodeKey.userTasks
  
  def stringify(intent: Int):String = "scriptTask----"

  def script(scriptImplementation: ScriptImplementation): ScriptTask =
    copy(scriptImplementation = Some(scriptImplementation))

  def groovy(scriptPath: ScriptPath): ScriptTask =
    copy(scriptImplementation = Some(ExternalScript(ScriptLanguage.Groovy,
      s"$scriptPath.groovy")))

  def inlineGroovy(script: String): ScriptTask =
    copy(scriptImplementation = Some(InlineScript(ScriptLanguage.Groovy,
      script)))

  def resultVariable(resultVariable: String): ScriptTask =
    copy(resultVariable = Some(resultVariable))

object ScriptTask:

  def apply(ident: Ident): ScriptTask =
    ScriptTask(Task(ident))

sealed trait ScriptImplementation:
  def language: ScriptLanguage

object ScriptImplementation:

  type ScriptPath = String

  case class InlineScript(language: ScriptLanguage,
                          script: String,
                         ) extends ScriptImplementation

  case class ExternalScript(language: ScriptLanguage,
                            resource: ScriptPath,
                           )extends ScriptImplementation :
    val deployResource = s"deployment://$resource"


// Extension methods define opaque types' public APIs
enum ScriptLanguage(val filePostfix: String):

  case Groovy extends ScriptLanguage(".groovy")

  case Javascript extends ScriptLanguage(".js")


