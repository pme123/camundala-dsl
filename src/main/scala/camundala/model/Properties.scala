package camundala.model

// Camunda Extension
case class Property(key: Ident, value: String)

case class Properties(properties: Seq[Property] = Seq.empty) {
}

object Properties {
  val none: Properties = Properties()
}
