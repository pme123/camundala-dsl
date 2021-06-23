package camundala
package model

opaque type VariableName = String

object VariableName:
  def apply(variableName: String): VariableName = variableName.replace("-", "__")

case class InOutParameter(name:Name, value: VariableAssignment | ScriptImplementation)

sealed trait VariableAssignment

object VariableAssignment:
  case class StringVal(value: String) extends VariableAssignment
  case class Expression(value: String) extends VariableAssignment
  
  object Expression :
    def apply(expr: String):Expression =
      new Expression(wrapExpression(expr))

trait HasInputParameters[T]:
  def inputParameters: Seq[InOutParameter]

  def withInputs(params: Seq[InOutParameter]): T

trait HasOutputParameters[T]:
  def outputParameters: Seq[InOutParameter]

  def withOutputs(params: Seq[InOutParameter]): T
