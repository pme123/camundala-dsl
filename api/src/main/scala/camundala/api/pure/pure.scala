package camundala
package api
package pure

import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema
import CamundaError.*

case class InOutDescr[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    id: String,
    descr: Option[String] | String = None,
    in: In = NoInput(),
    out: Out = NoOutput()
):

  lazy val maybeDescr = descr match
    case d: Option[String] => d
    case d: String => Some(d)

  lazy val maybeIn =
    if (in.isInstanceOf[Option[_]]) in else Some(in)

  lazy val maybeOut =
    if (out.isInstanceOf[Option[_]]) out else Some(out)

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
] extends InOut[In, Out, T]//:

 // def endpoint: api.ApiEndpoint[In, Out, T]

case class Process[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out],
    activitySeq: Seq[Activity[_, _, _]] = Seq.empty
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

case class DecisionDmn[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
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
      acts: Activity[_,_,_]*
                ) =
    Process(
      InOutDescr(id, descr, in, out),
      acts
    )

  extension[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](p: Process[In,Out])

    def activities(acts: Activity[_, _, _]*): Process[In,Out] =
      p.copy(activitySeq = acts)

  end extension

  def userTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](
     id: String,
     descr: Option[String] | String = None,
     in: In = NoInput(),
     out: Out = NoOutput(),
   ): UserTask[In, Out] =
    UserTask(
      InOutDescr(id, descr, in, out)
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
