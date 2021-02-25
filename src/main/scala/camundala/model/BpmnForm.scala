package camundala.model

import camundala.model._
import camundala.model.Constraint._
import camundala.model.GeneratedForm.FormField


sealed trait BpmnForm extends HasStringify

case class EmbeddedForm(formKey: FormKey)
  extends BpmnForm:
  
  def stringify(intent: Int):String =
    s"""${intentStr(intent)}form(
       |${formKey.stringify(intent + 1)}
       |${intentStr(intent)})""".stripMargin

opaque type FormKey = String

object FormKey:
  def apply(formKey: String): FormKey = formKey

  extension (formKey: FormKey)
    def stringify(intent: Int): String = s"""${intentStr(intent)}formKey("$formKey")"""

case class GeneratedForm(fields: Seq[FormField] = Seq.empty)
  extends BpmnForm :
  def fields(fld: FormField, flds: FormField*): GeneratedForm = copy(fields = (fields :+ fld) ++ flds)

  def stringify(intent: Int):String =
    s"""${intentStr(intent)}form(
       |----fields
       |${intentStr(intent)})""".stripMargin
       
object GeneratedForm:

  import FormFieldType._

  case class FormField(id: Ident,
                       label: String = "",
                       `type`: FormFieldType = StringType,
                       defaultValue: String = "",
                       values: EnumValues = EnumValues.none,
                       constraints: Constraints = Constraints.none,
                       properties: Properties = Properties.none):

    def fieldType(fieldType: FormFieldType): FormField = copy(`type` = fieldType)

    def label(l: String): FormField = copy(label = l)

    def default(d: String): FormField = copy(defaultValue = d)

    def value(key: Ident, value: String): FormField = copy(values = values :+ EnumValue(key, value))

    def prop(key: Ident, value: String): FormField = copy(properties = properties :+ Property(key, value))


  case class EnumValues(enums: Seq[EnumValue]):

    def :+(value: EnumValue): EnumValues = copy(enums :+ value)


  object EnumValues:
    def none: EnumValues = EnumValues(Seq.empty)

  case class EnumValue(key: Ident, label: String)


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

case class Constraints(constraints: Seq[Constraint]):
  def :+(constraint: Constraint): Constraints = copy(constraints = constraints :+ constraint)

object Constraints:
  def none = Constraints(Seq.empty)

sealed trait Constraint extends FieldAttr:
  def name: Ident

  def config: Option[String]

object Constraint:

  case class Custom(name: Ident, config: Option[String]) extends Constraint

  case object Required extends Constraint :
    val name: Ident = Ident("required")

    val config: Option[String] = None

  case object Readonly extends Constraint :
    val name: Ident = Ident("readonly")

    val config: Option[String] = None

  sealed trait MinMax extends Constraint :

    def value: Int

    val config: Option[String] = Some(s"$value")

  case class Minlength(value: Int)extends MinMax :
    val name: Ident = Ident("minlength")

  case class Maxlength(value: Int)extends MinMax :
    val name: Ident = Ident("maxlength")

  case class Min(value: Int)extends MinMax :
    val name: Ident = Ident("min")

  case class Max(value: Int)extends MinMax :
    val name: Ident = Ident("max")

