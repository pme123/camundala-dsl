package camundala
package gatling

import camundala.api.{CamundaProperty, CamundaVariable}
import camundala.api.CamundaVariable.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import camundala.bpmn.*
import camundala.domain.*
import camundala.gatling.TestOverrideType.*
import io.circe.{Decoder, Encoder}
import io.circe.Json.JArray
import sttp.tapir.Schema
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.jdk.CollectionConverters.*

case class TestOverride(
    key: String,
    overrideType: TestOverrideType, // problem with encoding?! derives JsonTaggedAdt.PureEncoder
    value: Option[CamundaVariable] = None
)

case class TestOverrides(overrides: Seq[TestOverride]) //Seq[TestOverride])

enum TestOverrideType:
  case Exists, NotExists, IsEquals, HasSize

/*
def overrides[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: InOut[In, Out, T]
](
    inOut: InOut[In, Out, T],
    key: String,
    overrideType: TestOverrideType,
    value: Option[CamundaVariable] = None
): InOutDescr[In, TestOverrides] =
  val testOverride = TestOverride(key,overrideType, value)
  val newOverrides: Seq[TestOverride] = inOut.out match
    case TestOverrides(overrides) =>
      overrides :+ testOverride
    case other =>
      Seq(testOverride)
  InOutDescr(
    inOut.id,
    inOut.in,
    TestOverrides(newOverrides),
    inOut.descr
  )
*/
def addOverride[
  T <: Product,
](
   model: T,
   key: String,
   overrideType: TestOverrideType,
   value: Option[CamundaVariable] = None
 ): TestOverrides =
  val testOverride = TestOverride(key,overrideType, value)
  val newOverrides: Seq[TestOverride] = model match
    case TestOverrides(overrides) =>
      overrides :+ testOverride
    case other =>
      Seq(testOverride)
  TestOverrides(newOverrides)


def statusCondition(status: Int*): Session => Boolean = session => {
  println("<<< lastStatus: " + session("lastStatus").as[Int])
  println("<<< retryCount: " + session("retryCount").as[Int])
  val lastStatus = session("lastStatus").as[Int]
  !status.contains(lastStatus)
}

def taskCondition(): Session => Boolean = session => {
  println("<<< retryCount: " + session("retryCount").as[Int])
  session.attributes.get("taskId").contains(null)
}

// check if the process is  not active
def processCondition: Session => Boolean = session =>
  println("<<< retryCount: " + session("retryCount").as[Int])
  val status = session.attributes.get("processState")
  status.contains("ACTIVE")

def extractJson(path: String, key: String) =
  jsonPath(path)
    .ofType[String]
    .transform { v =>
      println(s"<<< Extracted $key: $v"); v
    } // save the data
    .saveAs(key)

val printBody =
  bodyString.transform { b => println(s"<<< Response Body: $b") }

val printSession: ChainBuilder =
  exec { session =>
    println(s"<<< Session: " + session)
    session
  }

def checkProps[T <: Product](
    out: T,
    result: Seq[CamundaProperty]
): Boolean =
  out match
    case TestOverrides(overrides) =>
      check(overrides, result)
    case product =>
      check(product, result)

private def check(overrides: Seq[TestOverride], result: Seq[CamundaProperty]) =
  overrides
    .map {
      case TestOverride(k, Exists, _) =>
        val matches = result.exists(_.key == k)
        if (!matches)
          println(s"!!! $k did NOT exist in $result")
        matches
      case TestOverride(k, NotExists, _) =>
        val matches = !result.exists(_.key == k)
        if (!matches)
          println(s"!!! $k did EXIST in $result")
        matches
      case TestOverride(k, IsEquals, Some(v)) =>
        val r = result.find(_.key == k)
        val matches = r.nonEmpty && r.exists(_.value == v)
        if (!matches)
          println(s"!!! $v ($k) is NOT equal in $r")
        matches
      case TestOverride(k, HasSize, Some(value)) =>
        val r = result.find(_.key == k)
        val matches = r.exists {
          _.value match
            case CJson(j, _) =>
              (toJson(j).asArray, value) match
                case (Some(vector), CInteger(s, _)) =>
                  vector.size == s
                case _ =>
                  false
            case _ => false
        }
        if (!matches)
          println(s"!!! $k has NOT Size $value in $r")
        matches
      case other =>
        println(
          s"!!! Only ${TestOverrideType.values.mkString(", ")} for TestOverrides supported"
        )
        false
    }
    .forall(_ == true)

private def check[T <: Product](
    product: T,
    result: Seq[CamundaProperty]
): Boolean =
  product
    .asVarsWithoutEnums()
    .map { case key -> value =>
      result
        .find(_.key == key)
        .map { obj =>
          obj.value match
            case _: CFile =>
              println(
                s"<<< Files cannot be tested as its content is _null_ ('$key')."
              )
              true
            case CJson(v, _) =>
              import io.circe.syntax.*
              val matches = value match
                case it: java.lang.Iterable[?] =>
                  val setJson = toJson(v).as[Set[Json]].toOption
                    .getOrElse(s"Could not create Set of Json from $v")
                  setJson == it.asScala.toSet
                case s: Json =>
                  toJson(v) == s
                case other =>
                  println(
                    s"!!! Not expected Type: ${other.getClass} / $other"
                  )
                  throwErr(
                    s"Only Json and java.lang.Iterable[Json] allowed here. But was: ${other.getClass}."
                  )
              if (!matches)
                println(
                  s"!!! The Json value '${toJson(v).getClass} / ${toJson(v)}' of $key does not match the result variable ${value.getClass} / '$value'.\n $result"
                )
              matches
            case other =>
              val matches = obj.value.value == value
              if (!matches)
                println(s"OTHER: ${other.getClass}")
                println(
                  s"!!! The value '$value' of $key does not match the result variable '${obj.value.value}'.\n $result"
                )
              matches
        }
        .getOrElse {
          println(
            s"!!! $key does not exist in the result variables.\n $result"
          )
          false
        }
    }
    .forall(_ == true)
