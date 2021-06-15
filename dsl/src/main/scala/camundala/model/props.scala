package camundala.model

// Camunda Extension
case class Property(key: Ident, value: String)

case class Properties(properties: Seq[Property] = Seq.empty):
  def isEmpty = properties.isEmpty
  def nonEmpty = properties.nonEmpty
  def :+(prop: Property): Properties = copy(properties :+ prop)

object Properties {
  val none: Properties = Properties()
}

trait HasProperties[T]:
  def properties: Properties

  def prop(prop: Property): T
