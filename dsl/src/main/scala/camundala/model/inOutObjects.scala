package camundala
package model

import collection.JavaConverters.*

trait InOutObject extends Product:

  def names(): Seq[String] = productElementNames.toSeq

  def asVars(): Map[String, Any] =
    productElementNames
      .zip(productIterator)
      .toMap

  def asJavaVars(): java.util.Map[String, Any] =
    asVars().asJava

case object NoInputsOutputs extends InOutObject

object InOutObject:
  def none = NoInputsOutputs

trait HasInputObject[T]:
  def inputObject: InOutObject

  def withInput(input: InOutObject): T

trait HasOutputObject[T]:
  def outputObject: InOutObject

  def withOutput(output: InOutObject): T
