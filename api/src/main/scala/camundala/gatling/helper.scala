package camundala
package gatling

import camundala.api.CamundaProperty
import camundala.api.CamundaVariable.CFile
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import camundala.bpmn.*
import camundala.domain.*
import camundala.gatling.TestOverrideType.*
import io.circe.Encoder

case class TestOverride(
    key: String,
    overrideType: TestOverrideType, // problem with encoding?! derives JsonTaggedAdt.PureEncoder
    value: Option[String] = None
)

case class TestOverrides(overrides: Seq[TestOverride]) //Seq[TestOverride])

enum TestOverrideType:
  case Exists, NotExists

def statusCondition(status: Int*): Session => Boolean = session => {
  println(">>> lastStatus: " + session("lastStatus").as[Int])
  println(">>> retryCount: " + session("retryCount").as[Int])
  val lastStatus = session("lastStatus").as[Int]
  !status.contains(lastStatus)
}

def taskCondition(): Session => Boolean = session => {
  println(">>> retryCount: " + session("retryCount").as[Int])
  session.attributes.get("taskId").contains(null)
}

// check if the process is  not active
def processCondition(): Session => Boolean = session => {
  println(">>> retryCount: " + session("retryCount").as[Int])
  session.attributes.get("processState").equals("ACTIVE")
}

def extractJson(path: String, key: String) =
  jsonPath(path)
    .ofType[String]
    .transform { v =>
      println(s">>> Extracted $key: $v"); v
    } // save the data
    .saveAs(key)

val printBody =
  bodyString.transform { b => println(s">>> Response Body: $b") }

val printSession: ChainBuilder =
  exec { session =>
    println(s">>> Session: " + session)
    session
  }

def checkProps[T <: Product](
    out: T,
    result: Seq[CamundaProperty]
): Boolean = {
  out match
    case TestOverrides(overrides) =>
      overrides
        .map {
          case TestOverride(k, Exists, _) =>
            println(s"$k EXISTS! $result")
            result.exists(_.key == k)
          case TestOverride(k, NotExists, _) =>
            !result.exists(_.key == k)
          case other =>
            throwErr(
              s"Sorry only ${TestOverrideType.values.mkString(", ")} for TestOverrides supported"
            )
        }
        .forall(_ == true)
    case product =>
      product
        .asVarsWithoutEnums()
        .map { case key -> value =>
          result
            .find(_.key == key)
            .map { obj =>
              obj.value match
                case _: CFile =>
                  println(
                    s">>> Files cannot be tested as its content is _null_ ('$key')."
                  )
                  true
                case other =>
                  val matches = obj.value.value == value
                  if (!matches)
                    println(
                      s"!!! The value ' ${obj.value.value}' of $key does not match the result variable '$value'.\n $result"
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
}
