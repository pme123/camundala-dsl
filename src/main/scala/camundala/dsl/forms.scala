package camundala.dsl

import camundala.dsl.forms.{EnumFieldAttr, FieldAttr}
import camundala.model.Constraint._
import camundala.model.GeneratedForm.FormFieldType.{BooleanType, DateType, EnumType, LongType, StringType}
import camundala.model.GeneratedForm.{DefaultValue, EnumValue, EnumValues, FormField, FormFieldType, Label}
import camundala.model._

trait forms:
  def form(formRef: FormKey) =
    EmbeddedForm(formRef)

  def formKey(key: String): FormKey = FormKey(key)

  def form(formFields: FormField*) =
    GeneratedForm(formFields)

  def formField(id: Ident, fieldType: FormFieldType, fieldAttrs: EnumFieldAttr*): FormField =
    FormField(id,
      constraints = Constraints(fieldAttrs.collect { case c: Constraint => c }),
      properties = Properties(fieldAttrs.collect { case p: Property => p }),
      values = EnumValues(fieldAttrs.collect { case v: EnumValue => v }),
      label = fieldAttrs.collect { case l: Label => l }.headOption, // this works as it is the only opaque type
      defaultValue = fieldAttrs.collect { case v: String => DefaultValue(v) }.headOption, // this works as it is the only opaque type
      `type` = fieldType
    )

  def textField(id: Ident, fieldAttrs: FieldAttr*): FormField =
    formField(id, StringType, fieldAttrs: _*)

  def stringField(id: Ident, fieldAttrs: FieldAttr*): FormField =
    formField(id, StringType, fieldAttrs: _*)

  def booleanField(id: Ident, fieldAttrs: FieldAttr*): FormField =
    formField(id, BooleanType, fieldAttrs: _*)

  def longField(id: Ident, fieldAttrs: FieldAttr*): FormField =
    formField(id, LongType, fieldAttrs: _*)

  def dateField(id: Ident, fieldAttrs: FieldAttr*): FormField =
    formField(id, DateType, fieldAttrs: _*)

  def enumField(id: Ident, fieldAttrs: EnumFieldAttr*): FormField =
    formField(id, EnumType, fieldAttrs: _*)

  def enumValue(id: Ident, name: Name): EnumValue =
    EnumValue(id, name)

  def defaultValue(value: String): DefaultValue =
    DefaultValue(value)

  def label(value: String): Label =
    Label(value)

object forms:

  type FieldAttr = Property | Constraint | DefaultValue | Label

  type EnumFieldAttr = FieldAttr | EnumValue

  trait constraints:
    def readonly: Constraint = Readonly

    def required: Constraint = Required

    def minlength(value: Int): Constraint = Minlength(value)

    def maxlength(value: Int): Constraint = Maxlength(value)

    def min(value: Int): Constraint = Min(value)

    def max(value: Int): Constraint = Max(value)

    def custom(name: Ident): Constraint = Custom(name)
    def custom(name: Ident, value: String): Constraint = Custom(name, Some(value))
