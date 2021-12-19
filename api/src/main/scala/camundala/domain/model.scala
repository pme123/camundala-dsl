package camundala
package domain

import scala.jdk.CollectionConverters.*

case class NoInput()
case class NoOutput()

extension (product: Product)
  def names(): Seq[String] = product.productElementNames.toSeq

  def asVars(): Map[String, Any] =
    product.productElementNames
      .zip(product.productIterator)
      .toMap

  def asJavaVars(): java.util.Map[String, Any] =
    asVars().asJava

  def asDmnVars(): Map[String, Any] =
    asVars()
      .map {
        case (k, v: scala.reflect.Enum) =>
          (k, v.toString)
        case (k, v) => (k, v)
      }
end extension