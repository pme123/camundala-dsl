package camundala
package dsl

trait dmns:

  def dmn(ident: String): Dmn = Dmn(Ident(ident))
  def dmn(dmnFile: File): Dmn = Dmn(Ident(dmnFile))

