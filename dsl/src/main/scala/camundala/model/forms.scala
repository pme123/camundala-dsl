package camundala.model

import camundala.dsl.forms.{EnumFieldAttr, FieldAttr}
import camundala.model.*
import camundala.model.Constraint.*
import camundala.model.GeneratedForm.*
import scala.annotation.targetName

sealed trait BpmnForm

case class EmbeddedForm(formKey: FormKey) extends BpmnForm

opaque type FormKey = String

object FormKey:
  def apply(formKey: String): FormKey = formKey

case class EmbeddedStaticForm(private val formPath: BpmnPath) extends BpmnForm

object EmbeddedStaticForm:
  @targetName("create")
  def apply(path: String): EmbeddedStaticForm =
    println(s"EmbeddedStaticForm: $path")
    new EmbeddedStaticForm(
      BpmnPath(
        if (path.startsWith("embedded:app:"))
          path
        else
          s"embedded:app:$path"
      )
    )

case class GeneratedForm(fields: Seq[FormField] = Seq.empty) extends BpmnForm

object GeneratedForm:

  import FormFieldType._

  case class FormField(
      id: Ident,
      label: Option[Label] = None,
      `type`: FormFieldType = StringType,
      defaultValue: Option[DefaultValue] = None,
      values: EnumValues = EnumValues.none,
      constraints: Constraints = Constraints.none,
      properties: Properties = Properties.none
  ) extends HasProperties[FormField]:

    def prop(prop: Property): FormField =
      copy(properties = properties :+ prop)

  case class Label(str: String)

  opaque type DefaultValue = String

  object DefaultValue:
    def apply(value: String): DefaultValue = value

  case class EnumValues(enums: Seq[EnumValue]):
    def :+(value: EnumValue) = EnumValues(enums :+ value)

  object EnumValues:
    def none: EnumValues = EnumValues(Seq.empty)

  case class EnumValue(id: Ident, name: Name)

  sealed trait FormFieldType:
    def name: String

  object FormFieldType:

    case object StringType extends FormFieldType:
      val name = "string"

    case object BooleanType extends FormFieldType:
      val name = "boolean"

    case object EnumType extends FormFieldType:
      val name = "enum"

    case object LongType extends FormFieldType:
      val name = "long"

    case object DateType extends FormFieldType:
      val name = "date"

case class Constraints(constraints: Seq[Constraint]):
  def :+(constraint: Constraint) = Constraints(constraints :+ constraint)

object Constraints:
  def none = Constraints(Seq.empty)

sealed trait Constraint:
  def name: Ident

  def config: Option[String]

object Constraint:

  case class Custom(name: Ident, config: Option[String] = None)
      extends Constraint

  case object Required extends Constraint:
    val name: Ident = Ident("required")

    val config: Option[String] = None

  case object Readonly extends Constraint:
    val name: Ident = Ident("readonly")

    val config: Option[String] = None

  sealed trait MinMax extends Constraint:

    def value: Int

    val config: Option[String] = Some(s"$value")

  case class Minlength(value: Int) extends MinMax:
    val name: Ident = Ident("minlength")

  case class Maxlength(value: Int) extends MinMax:
    val name: Ident = Ident("maxlength")

  case class Min(value: Int) extends MinMax:
    val name: Ident = Ident("min")

  case class Max(value: Int) extends MinMax:
    val name: Ident = Ident("max")

trait HasMaybeForm[T]:
  def maybeForm: Option[BpmnForm]

  def withForm(form: BpmnForm): T