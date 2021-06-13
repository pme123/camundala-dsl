package camundala
package dsl

trait events:

  def startEvent(ident: String) =
    StartEvent(ident)

  def endEvent(ident: String) =
    EndEvent(ident)


