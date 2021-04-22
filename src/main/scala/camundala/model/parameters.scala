package camundala.model

opaque type VariableName = Ident

case class InOutParameter(name:Name, value: VariableAssignment | ScriptImplementation)

sealed trait VariableAssignment

object VariableAssignment:
  case class StringVal(value: String) extends VariableAssignment
  case class Expression(value: String) extends VariableAssignment
  object Expression :
    def apply(expr: String):Expression =
      new Expression(
        if(expr.startsWith("$"))
          expr
        else
          s"$${$expr}"
      )
