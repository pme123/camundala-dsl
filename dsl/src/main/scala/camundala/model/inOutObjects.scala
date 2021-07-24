package camundala
package model


trait InOutObject extends Product

case object NoInputsOutputs extends InOutObject

object InOutObject:
  def none = NoInputsOutputs

trait HasInputObject[T]:
  def inputObject: InOutObject

  def withInput(input: InOutObject): T


trait HasOutputObject[T]:
  def outputObject: InOutObject

  def withOutput(output: InOutObject): T
