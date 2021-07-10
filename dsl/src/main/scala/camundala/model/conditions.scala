package camundala.model

sealed trait Condition

object Condition:

  case class ExpressionCond(private val expr: String) extends Condition

  case class InlineScriptCond(
      script: String,
      format: ScriptLanguage = ScriptLanguage.Groovy
  ) extends Condition

  case class ScriptCond(
      ref: ScriptImplementation.ScriptPath,
      format: ScriptLanguage = ScriptLanguage.Groovy
  ) extends Condition:
    val deployResource = s"deployment://$ref"
