package camundala.model

import camundala.model.BpmnProcess.NodeKey
import camundala.model.ScriptImplementation.{ExternalScript, InlineScript, ScriptPath}
import camundala.model.TaskImplementation.Expression

case class ScriptTask(task: Task,
                      scriptImplementation: ScriptImplementation,
                      resultVariable: Option[Ident] = None)
  extends HasTask
    with ProcessElement :
  val elemType = NodeKey.userTasks
  
  def stringify(intent: Int):String = "scriptTask----"

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


