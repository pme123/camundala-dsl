package camundala
package bpmn

import domain.*

import scala.deriving.Mirror
import scala.compiletime.{constValue, constValueTuple}

case class InOutDescr[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    id: String,
    in: In = NoInput(),
    out: Out = NoOutput(),
    descr: Option[String] | String = None
):

  lazy val maybeDescr = descr match
    case d: Option[String] => d
    case d: String => Some(d)

sealed trait InOut[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: InOut[In, Out, T]
]:

  def inOutDescr: InOutDescr[In, Out]

  lazy val id = inOutDescr.id
  lazy val descr = inOutDescr.descr
  lazy val in = inOutDescr.in
  lazy val out = inOutDescr.out

  def withInOutDescr(inOutDescr: InOutDescr[In, Out]): T

  def withId(i: String): T =
    withInOutDescr(inOutDescr.copy(id = i))

  def withDescr(description: String): T =
    withInOutDescr(inOutDescr.copy(descr = Some(description)))

  def withIn(in: In): T =
    withInOutDescr(inOutDescr.copy(in = in))

  def withOut(out: Out): T =
    withInOutDescr(
      inOutDescr.copy(out = out)
    )

sealed trait Activity[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: InOut[In, Out, T]
] extends InOut[In, Out, T] //:

// def endpoint: api.ApiEndpoint[In, Out, T]

case class Process[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends InOut[In, Out, Process[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): Process[In, Out] =
    copy(inOutDescr = descr)

case class UserTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends Activity[In, Out, UserTask[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): UserTask[In, Out] =
    copy(inOutDescr = descr)

enum HitPolicy:

  case UNIQUE
  case FIRST
  case PRIORITY
  case ANY
  case COLLECT
  case COLLECT_SUM
  case COLLECT_MIN
  case COLLECT_MAX
  case COLLECT_COUNT
  case RULE_ORDER
  case OUTPUT_ORDER

  def hasManyResults =
    Seq(COLLECT, RULE_ORDER, OUTPUT_ORDER).contains(this)

end HitPolicy

type DmnValueType = String | Boolean | Int | Long | Double

case class DecisionDmn[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    hitPolicy: HitPolicy,
    inOutDescr: InOutDescr[In, Out]
) extends Activity[In, Out, DecisionDmn[In, Out]]:

  lazy val decisionDefinitionKey: String = inOutDescr.id

  def withInOutDescr(descr: InOutDescr[In, Out]): DecisionDmn[In, Out] =
    copy(inOutDescr = descr)

  def decisionResultType: DecisionResultType = {
    (hasManyOutputVars(inOutDescr.out), hitPolicy.hasManyResults) match
      case (false, false) =>
        DecisionResultType.singleEntry
      case (false, true) =>
        DecisionResultType.collectEntries
      case (true, false) =>
        DecisionResultType.singleResult
      case (true, true) =>
        DecisionResultType.resultList
  }

extension (output: Product)
  def isSingleEntry =
    output.productIterator.toList match
      case (_: DmnValueType) :: Nil => true
      case _ => false
end extension // Product

def hasManyOutputVars(output: Product) =
  println(s"outputxxx: $output")
  if (output.productIterator.size > 1)
    true // SingleResult
  else
    output.productIterator.next() match
      case p: Iterable[?] =>
        p.head match
          case p: Product => true
          case o => false
      case p: Product =>
        p.productIterator.size > 1
      case o => false

enum DecisionResultType:
  case singleEntry // TypedValue
  case singleResult // Map(String, Object)
  case collectEntries // List(Object)
  case resultList // List(Map(String, Object))

case class CallActivity[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends Activity[In, Out, CallActivity[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): CallActivity[In, Out] =
    copy(inOutDescr = descr)

object CallActivity:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): CallActivity[In, Out] =
    CallActivity(process.inOutDescr)

case class ServiceTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends Activity[In, Out, ServiceTask[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): ServiceTask[In, Out] =
    copy(inOutDescr = descr)

trait PureDsl:

  def process[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ) =
    Process(
      InOutDescr(id, in, out, descr)
    )

  def userTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): UserTask[In, Out] =
    UserTask(
      InOutDescr(id, in, out, descr)
    )

  def dmn[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      hitPolicy: HitPolicy,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ) =
    DecisionDmn[In, Out](
      hitPolicy,
      InOutDescr(decisionDefinitionKey, in, out, descr)
    )

  def singleEntry[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      hitPolicy: HitPolicy,
      in: In,
      out: Out
  ) =
    require(
      out.isSingleEntry,
      "A singleEntry must look like `case class SingleEntry(result: DmnValueType)`"
    )
    require(
      !hitPolicy.hasManyResults,
      "The Hitpolicy must have only one Result, like UNIQUE, COLLECT_SUM"
    )
    dmn(decisionDefinitionKey, hitPolicy, in, out)

  def serviceTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): ServiceTask[In, Out] =
    ServiceTask(
      InOutDescr(id, in, out, descr)
    )

  inline def enumDescr[E](
      descr: Option[String] = None
  )(using m: Mirror.SumOf[E]): String =
    val name = constValue[m.MirroredLabel]
    val values =
      constValueTuple[m.MirroredElemLabels].productIterator.mkString(", ")
    val enumDescription =
      s"Enumeration $name: \n- $values"
    descr
      .map(_ + s"\n\n$enumDescription")
      .getOrElse(enumDescription)
