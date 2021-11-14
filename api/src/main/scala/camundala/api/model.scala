package camundala
package api

import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

case class InOutDescr[
  In <: Product: Encoder: Decoder: Schema,
  Out <: Product: Encoder: Decoder: Schema
](
   id: String,
   descr: Option[String] | String = None,
   in: In = NoInput(),
   out: Out = NoOutput(),
   hasManyOuts: Boolean
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

  def withOutExample(out: Out): T =
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
   inOutDescr: InOutDescr[In, Out],
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
  case ANY
  case COLLECT
  case RULE_ORDER

  def hasManyResults = !Seq(UNIQUE, FIRST, ANY).contains(this)
end HitPolicy

case class DecisionDmn[
  In <: Product: Encoder: Decoder: Schema,
  Out <: Product: Encoder: Decoder: Schema
](
   decisionDefinitionKey: String,
   hitPolicy: HitPolicy,
   inOutDescr: InOutDescr[In, Out]
 ) extends Activity[In, Out, DecisionDmn[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): DecisionDmn[In, Out] =
    copy(inOutDescr = descr)

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

trait PureDsl:

  def process[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](
     id: String,
     descr: Option[String] | String = None,
     in: In = NoInput(),
     out: Out = NoOutput(),
   ) =
    Process(
      InOutDescr(id, descr, in, out, false)
    )

  def userTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](
     id: String,
     descr: Option[String] | String = None,
     in: In = NoInput(),
     out: Out = NoOutput()
   ): UserTask[In, Out] =
    UserTask(
      InOutDescr(id, descr, in, out, false)
    )

  def dmn[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](
     decisionDefinitionKey: String,
     hitPolicy: HitPolicy,
     id: String,
     descr: Option[String] | String = None,
     in: In = NoInput(),
     out: Out = NoOutput()
   ) =
    DecisionDmn[In, Out](
      decisionDefinitionKey,
      hitPolicy,
      InOutDescr(id, descr, in, out, hitPolicy.hasManyResults)
    )

  import reflect.Selectable.reflectiveSelectable
  def enumDescr(
                 enumeration: { def values: Array[?] },
                 descr: Option[String] = None
               ) =
    val enumDescription =
      s"Enumeration: \n- ${enumeration.values.mkString("\n- ")}"
    descr
      .map(_ + s"\n\n$enumDescription")
      .getOrElse(enumDescription)

