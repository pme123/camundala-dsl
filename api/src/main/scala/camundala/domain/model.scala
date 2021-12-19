package camundala
package domain

import io.circe.{ACursor, Decoder, Encoder, HCursor, Json}
import sttp.tapir.{Schema, SchemaType}
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.generic.Derived
import sttp.tapir.generic.auto.*

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

case class ManyInOut[
    T <: Product: Encoder: Decoder: Schema
](inOut: T, examples: T*):
  def toSeq: Seq[T] = inOut +: examples


object ManyInOut:
  def apply[
      T <: Product: Encoder: Decoder: Schema
  ](inOuts: Seq[T]): ManyInOut[T] =
    ManyInOut(inOuts.head, inOuts.tail: _*)

import io.circe.Encoder
import io.circe.generic.auto.*

case class MyExample(someStr: String)

def doIt[
    Out <: Product: Encoder: Decoder: Schema
](
    out: Out
) = println(s"Auto: $out")

implicit def encodeManyInOut[
    T <: Product: Encoder: Decoder: Schema
]: Encoder[ManyInOut[T]] = new Encoder[ManyInOut[T]] {
  final def apply(a: ManyInOut[T]): Json =
    Json.arr(
      (a.inOut.asJson +: a.examples.map(_.asJson)): _*
    ) //Seq(a.inOut, a.examples).map(_.asJson))
}

implicit def decodeManyInOut[
    T <: Product: Encoder: Decoder: Schema
]: Decoder[ManyInOut[T]] = new Decoder[ManyInOut[T]] {
  final def apply(c: HCursor): Decoder.Result[ManyInOut[T]] =
    for {
      arr <- c.as[Seq[T]]
    } yield {
      ManyInOut(arr)
    }
}

implicit def schemaForNel[T <: Product: Encoder: Decoder: Schema]: Schema[ManyInOut[T]] =
  Schema[ManyInOut[T]](SchemaType.SArray(implicitly[Schema[T]])(_.toSeq))

object runner extends App:
  doIt(ManyInOut(MyExample("hello")))
