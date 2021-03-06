package camundala.model

import camundala.dsl.forms.{EnumFieldAttr, FieldAttr}
import camundala.model.*
import camundala.model.Constraint.*
import camundala.model.GeneratedForm.*

sealed trait BpmnForm extends HasStringify

case class EmbeddedForm(formKey: FormKey)
  extends BpmnForm :

  def stringify(intent: Int): String =
    stringifyElements(intent, "form", formKey.stringify(1))

opaque type FormKey = String

object FormKey:
  def apply(formKey: String): FormKey = formKey

  extension (formKey: FormKey)
    def stringify(intent: Int): String = s"""${intentStr(intent)}formKey("$formKey")"""

case class GeneratedForm(fields: Seq[FormField] = Seq.empty)
  extends BpmnForm :

  def stringify(intent: Int): String =
    stringifyWrap(intent, "form", fields)

object GeneratedForm:

  import FormFieldType._

  case class FormField(id: Ident,
                       label: Option[Label] = None,
                       `type`: FormFieldType = StringType,
                       defaultValue: Option[DefaultValue] = None,
                       values: EnumValues = EnumValues.none,
                       constraints: Constraints = Constraints.none,
                       properties: Properties = Properties.none)
    extends HasStringify :

    def stringify(intent: Int): String =
      stringifyElements(intent + 1, `type`.name + "Field",
        Seq(id.stringify(1)) ++
          constraints.constraints.map(_.stringify(1)) ++
          label.map(_.stringify(1)).toSeq ++
          defaultValue.map(_.stringify(1)).toSeq: _*)

  case class Label(str: String):
    def stringify(intent: Int): String = s"""${intentStr(intent)}label("$str")"""

  opaque type DefaultValue = String

  object DefaultValue:
    def apply(value: String): DefaultValue = value

  extension (value: DefaultValue)
    def stringify(intent: Int): String = s"""${intentStr(intent)}defaultValue("$value")"""

  case class EnumValues(enums: Seq[EnumValue])

  object EnumValues:
    def none: EnumValues = EnumValues(Seq.empty)

  case class EnumValue(id: Ident, name: Name)


  sealed trait FormFieldType:
    def name: String

  object FormFieldType:

    case object StringType extends FormFieldType :
      val name = "string"

    case object BooleanType extends FormFieldType :
      val name = "boolean"

    case object EnumType extends FormFieldType :
      val name = "enum"

    case object LongType extends FormFieldType :
      val name = "long"

    case object DateType extends FormFieldType :
      val name = "date"

case class Constraints(constraints: Seq[Constraint])

object Constraints:
  def none = Constraints(Seq.empty)

sealed trait Constraint
  extends HasStringify :
  def name: Ident

  def config: Option[String]

object Constraint:

  case class Custom(name: Ident, config: Option[String] = None)
    extends Constraint :

    def stringify(intent: Int): String = config match {
      case None => s"""${intentStr(intent)}custom(${name.stringify(0)})"""
      case Some(value) => s"""${intentStr(intent)}custom(${name.stringify(0)}, "$value")"""
    }

  case object Required extends Constraint :
    val name: Ident = Ident("required")

    val config: Option[String] = None

    def stringify(intent: Int): String = s"${intentStr(intent)}required"


  case object Readonly extends Constraint :
    val name: Ident = Ident("readonly")

    val config: Option[String] = None

    def stringify(intent: Int): String = s"${intentStr(intent)}readonly"

  sealed trait MinMax extends Constraint :

    def value: Int

    val config: Option[String] = Some(s"$value")

    def stringify(intent: Int): String = s"${intentStr(intent)}$name($value)"

  case class Minlength(value: Int)extends MinMax :
    val name: Ident = Ident("minlength")

  case class Maxlength(value: Int)extends MinMax :
    val name: Ident = Ident("maxlength")

  case class Min(value: Int)extends MinMax :
    val name: Ident = Ident("min")

  case class Max(value: Int)extends MinMax :
    val name: Ident = Ident("max")

