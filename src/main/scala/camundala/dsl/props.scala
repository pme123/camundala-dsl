package camundala.dsl

import camundala.model._

trait props :
  
  extension[T] (hasProperty: HasProperties[T])
    def prop(key: String, value: String) =
     hasProperty.prop(Property(Ident(key), value))
