package camundala.model

sealed trait Condition

object Condition:

  case class ExpressionCond(expr: String) extends Condition

  case class ScriptCond(script: String, format: ScriptLanguage) extends Condition

  case class ScriptRefCond(ref: String, format: ScriptLanguage) extends Condition
