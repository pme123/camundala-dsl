package camundala.model

import camundala.model.ScriptImplementation.ScriptPath

sealed trait Condition

object Condition:

  case class ExpressionCond(expr: String)
    extends Condition

  case class InlineScriptCond(script: String, format: ScriptLanguage = ScriptLanguage.Groovy)
    extends Condition

  case class ScriptCond(ref: ScriptPath, format: ScriptLanguage = ScriptLanguage.Groovy)
    extends Condition :
    val deployResource = s"deployment://$ref"

