package camundala.model

import camundala.model.Constraint._
import camundala.model.GeneratedForm.FormField


sealed trait BpmnForm

case class EmbeddedForm(formRef: Ident)
  extends BpmnForm

case class GeneratedForm(fields: Seq[FormField] = Seq.empty)
  extends BpmnForm :
  def fields(fld: FormField, flds: FormField*): GeneratedForm = copy(fields = (fields :+ fld) ++ flds)


object GeneratedForm:

  import FormFieldType._

  def textField(id: Ident): FormField =
    FormField(id)

  def booleanField(id: Ident): FormField =
    FormField(id)
      .fieldType(BooleanType)

  def longField(id: Ident): FormField =
    FormField(id)
      .fieldType(LongType)

  def dateField(id: Ident): FormField =
    FormField(id)
      .fieldType(DateType)

  def enumField(id: Ident): FormField =
    FormField(id)
      .fieldType(EnumType)

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

    def constraint(constraint: Constraint): FormField =
      copy(constraints = constraints :+ constraint)

    def readonly: FormField =  constraint (Readonly)

    def required: FormField =  constraint (Required)

    def minlength(value: Int): FormField =  constraint (Minlength(value))

    def maxlength(value: Int): FormField =  constraint (Maxlength(value))

    def min(value: Int): FormField =  constraint (Min(value))

    def max(value: Int): FormField =  constraint (Max(value))

    def custom(name: Ident, config: Option[String]): FormField =  constraint (Custom(name, config))

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

sealed trait Constraint:
  def name: Ident

  def config: Option[String]

object Constraint:

  case class Custom(name: Ident, config: Option[String]) extends Constraint

  case object Required extends Constraint :
    val name: Ident = "required"

    val config: Option[String] = None

  case object Readonly extends Constraint :
    val name: Ident = "readonly"

    val config: Option[String] = None

  sealed trait MinMax extends Constraint :

    def value: Int

    val config: Option[String] = Some(s"$value")

  case class Minlength(value: Int)extends MinMax :
    val name: Ident = "minlength"

  case class Maxlength(value: Int)extends MinMax :
    val name: Ident = "maxlength"

  case class Min(value: Int)extends MinMax :
    val name: Ident = "min"

  case class Max(value: Int)extends MinMax :
    val name: Ident = "max"

