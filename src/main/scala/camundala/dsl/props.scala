package camundala.dsl

import camundala.model._

trait props :
  def prop(key: Ident, value: String) =
    Property(key, value)
