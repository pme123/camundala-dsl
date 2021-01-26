package camundala.model

import camundala.model.GeneratedForm.{EnumValue, EnumValues}

// Camunda Extension
case class Property(key: Ident, value: String)

case class Properties(properties: Seq[Property] = Seq.empty):
  
  def :+(prop: Property): Properties = copy(properties :+ prop)

object Properties {
  val none: Properties = Properties()
}
