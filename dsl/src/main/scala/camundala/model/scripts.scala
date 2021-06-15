package camundala
package model

case class ScriptTask(task: Task,
                      scriptImplementation: ScriptImplementation = ScriptImplementation.InlineScript(ScriptLanguage.Groovy, ""),
                      resultVariable: Option[Ident] = None
                     )
  extends HasTask[ScriptTask] :
  val elemKey = ElemKey.userTasks

  def withTask(task: Task): ScriptTask = copy(task = task)

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


