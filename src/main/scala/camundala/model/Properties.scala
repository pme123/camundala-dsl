package camundala.model

trait FieldAttr

// Camunda Extension
case class Property(key: Ident, value: String) extends FieldAttr

case class Properties(properties: Seq[Property] = Seq.empty):
  
  def :+(prop: Property): Properties = copy(properties :+ prop)

object Properties {
  val none: Properties = Properties()
}
