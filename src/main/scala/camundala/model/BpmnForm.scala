package camundala.model

import camundala.model.GeneratedForm.FormField


sealed trait BpmnForm

case class EmbeddedForm(formRef: Ident)
  extends BpmnForm

case class GeneratedForm(fields: Seq[FormField] = Seq.empty)
  extends BpmnForm {
  def fields(fld: FormField, flds: FormField*): GeneratedForm = copy(fields = (fields :+ fld) ++ flds)

}

object GeneratedForm {

  import FormFieldType._

  def textField(id: Ident): SimpleField =
    SimpleField(id)

  def booleanField(id: Ident): SimpleField =
    SimpleField(id, `type` = BooleanType)

  def longField(id: Ident): SimpleField =
    SimpleField(id, `type` = LongType)

  def dateField(id: Ident): SimpleField =
    SimpleField(id, `type` = DateType)

  def enumField(id: Ident): EnumField =
    EnumField(id)


  sealed trait FormField

  case class SimpleField(id: Ident,
                         label: String = "",
                         `type`: FormFieldType = StringType,
                         defaultValue: String = "",
                         constraints: Constraints = Constraints.none,
                         properties: Properties = Properties.none)
    extends FormField {

    def fieldType(fieldType: FormFieldType): SimpleField = copy(`type` = fieldType)

    def label(l: String): SimpleField = copy(label = l)

    def default(d: String): SimpleField = copy(defaultValue = d)

  }

  case class EnumField(simpleField: SimpleField,
                       values: EnumValues = EnumValues.none
                      )
    extends FormField {
    val `type`: FormFieldType = EnumType

    def label(l: String): EnumField = copy(simpleField = simpleField.label(l))

    def default(d: String): EnumField = copy(simpleField = simpleField.default(d))

    def value(key: Ident, value: String): EnumField = copy(values = values :+ EnumValue(key, value))
  }

  object EnumField {
    def apply(id: Ident): EnumField =
      EnumField(SimpleField(id))
  }

  case class EnumValues(enums: Seq[EnumValue]) {

    def :+(value: EnumValue): EnumValues = copy(enums :+ value)

  }

  object EnumValues {
    def none: EnumValues = EnumValues(Seq.empty)
  }

  case class EnumValue(key: Ident, label: String)


  sealed trait FormFieldType {
    def name: String
  }

  object FormFieldType {

    case object StringType extends FormFieldType {
      val name = "string"
    }

    case object BooleanType extends FormFieldType {
      val name = "boolean"
    }

    case object EnumType extends FormFieldType {
      val name = "enum"
    }

    case object LongType extends FormFieldType {
      val name = "long"
    }

    case object DateType extends FormFieldType {
      val name = "date"
    }

  }

}

case class Constraints(constraints: Seq[Constraint])

object Constraints {
  def none = Constraints(Seq.empty)
}

sealed trait Constraint {
  def name: Ident

  def config: Option[String]
}

object Constraint {

  case class Custom(name: Ident, config: Option[String]) extends Constraint

  case object Required extends Constraint {
    val name: Ident = "required"

    val config: Option[String] = None
  }

  case object Readonly extends Constraint {
    val name: Ident = "readonly"

    val config: Option[String] = None
  }

  sealed trait MinMax extends Constraint {

    def value: Int

    val config: Option[String] = Some(s"$value")
  }

  case class Minlength(value: Int) extends MinMax {
    val name: Ident = "minlength"
  }

  case class Maxlength(value: Int) extends MinMax {
    val name: Ident = "maxlength"
  }

  case class Min(value: Int) extends MinMax {
    val name: Ident = "min"
  }

  case class Max(value: Int) extends MinMax {
    val name: Ident = "max"
  }

}
