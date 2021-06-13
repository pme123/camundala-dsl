package camundala
package dsl

trait props :
  
  extension[T] (hasProperty: HasProperties[T])
    def prop(key: String, value: String) =
     hasProperty.prop(Property(Ident(key), value))
