package camundala
package model


sealed trait InOutVariable:
  def local: Boolean

object InOutVariable:
  case class Source(source: VariableName, target: VariableName, local: Boolean = false) extends InOutVariable
  case class SourceExpression(sourceExpression: String, target: VariableName, local: Boolean = false) extends InOutVariable
  case class All(local: Boolean = false) extends InOutVariable

trait HasInVariables[T]:
  def inVariables: Seq[InOutVariable]

  def withIns(params: Seq[InOutVariable]): T

trait HasOutVariables[T]:
  def outVariables: Seq[InOutVariable]

  def withOuts(params: Seq[InOutVariable]): T


