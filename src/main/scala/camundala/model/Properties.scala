package camundala.model

import camundala.dsl.forms.FieldAttr

// Camunda Extension
case class Property(key: Ident, value: String) 
  extends HasStringify :
  def stringify(intent: Int = 0) =
    s"""prop(${key.stringify()}, "$value")"""

case class Properties(properties: Seq[Property] = Seq.empty):
  
  def :+(prop: Property): Properties = copy(properties :+ prop)

object Properties {
  val none: Properties = Properties()
}
