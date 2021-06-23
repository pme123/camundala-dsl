package camundala
package model


sealed trait InOutVariable

object InOutVariable:
  case class Source(source: VariableName, target: VariableName) extends InOutVariable
  case class SourceExpression(private val sourceExpression: String, target: VariableName) extends InOutVariable
  case object All extends InOutVariable

  object SourceExpression :
    def apply(expr: String, target: VariableName):SourceExpression =
      new SourceExpression(wrapExpression(expr), target)

trait HasInVariables[T]:
  def inVariables: Seq[InOutVariable]

  def withIns(params: Seq[InOutVariable]): T

trait HasOutVariables[T]:
  def outVariables: Seq[InOutVariable]

  def withOuts(params: Seq[InOutVariable]): T


