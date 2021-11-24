package camundala
package domain

import io.circe.Json
import org.latestbit.circe.adt.codec.JsonTaggedAdt
import org.latestbit.circe.adt.codec.impl.{JsonPureTaggedAdtEncoder, JsonPureTaggedAdtEncoderWithConfig}

import scala.deriving.Mirror

export io.circe.{Decoder, Encoder, Json}

case class NoInput()
case class NoOutput()
/*
trait EnumTrait[T]:
  extension (x: T) def values: Array[T]


object EnumTrait:


  implicit inline given derived[T]( using
                                    m: Mirror.Of[T]
                                  ): EnumTrait[T] = {
    new EnumTrait[T] {
      def apply[T](using m: EnumTrait[T]) = m
      def values: Array[T] =
        Mirror.SumOf[T].
    }
  }
//type DomainIn  = Product with Encoder with Decoder with Schema
//  Out <: Product: Encoder: Decoder: Schema
*/