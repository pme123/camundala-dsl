package camundala
package dsl

import dsl.forms.{EnumFieldAttr, FieldAttr}
import model.{FormKey, Ident, Name}
import GeneratedForm.FormFieldType.*

trait forms:

  extension[T](hasForm: HasMaybeForm[T])
    def form(formRef: FormKey): T =
      hasForm.withForm(EmbeddedForm(formRef))

    def staticForm(path: String): T =
      hasForm.withForm(EmbeddedStaticForm(path))

    def form(formFields: FormField*): T =
      hasForm.withForm(GeneratedForm(formFields))

  def formKey(key: String): FormKey = FormKey(key)

  def formField(id: String, fieldType: FormFieldType): FormField =
    FormField(
      Ident(id),
      /*   constraints = Constraints(fieldAttrs.collect { case c: Constraint => c }),
      properties = Properties(fieldAttrs.collect { case p: Property => p }),
      values = EnumValues(fieldAttrs.collect { case v: EnumValue => v }),
      label = fieldAttrs.collect { case l: Label => l }.headOption, // this works as it is the only opaque type
      defaultValue = fieldAttrs.collect { case v: String => DefaultValue(v) }.headOption, // this works as it is the only opaque type
       */ `type` = fieldType
    )

  def textField(id: String): FormField =
    formField(id, StringType)

  def stringField(id: String): FormField =
    formField(id, StringType)

  def booleanField(id: String): FormField =
    formField(id, BooleanType)

  def longField(id: String): FormField =
    formField(id, LongType)

  def dateField(id: String): FormField =
    formField(id, DateType)

  def enumField(id: String): FormField = //TODO enumField
    formField(id, EnumType)

  extension (formField: FormField)
    def enumValue(id: String, name: String): FormField =
      formField.copy(values =
        formField.values :+ EnumValue(Ident(id), Name(name))
      )

    def defaultValue(value: String): FormField =
      formField.copy(defaultValue = Some(DefaultValue(value)))

    def label(value: String): FormField =
      formField.copy(label = Some(Label(value)))

object forms:

  type FieldAttr = Property | Constraint | DefaultValue | Label

  type EnumFieldAttr = FieldAttr | EnumValue

  trait constraints:
    import Constraint.*
    extension (field: FormField)
      def readonly: FormField =
        field.copy(constraints = field.constraints :+ Readonly)

      def required: FormField =
        field.copy(constraints = field.constraints :+ Required)

      def minlength(value: Int): FormField =
        field.copy(constraints = field.constraints :+ Minlength(value))

      def maxlength(value: Int): FormField =
        field.copy(constraints = field.constraints :+ Maxlength(value))

      def min(value: Int): FormField =
        field.copy(constraints = field.constraints :+ Min(value))

      def max(value: Int): FormField =
        field.copy(constraints = field.constraints :+ Max(value))

      def custom(name: Ident): FormField =
        field.copy(constraints = field.constraints :+ Custom(name))
      def custom(name: Ident, value: String): FormField =
        field.copy(constraints = field.constraints :+ Custom(name, Some(value)))
