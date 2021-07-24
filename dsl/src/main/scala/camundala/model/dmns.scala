package camundala.model

case class Dmns(dmns: Seq[Dmn]) :

  def :+(dmn: Dmn): Dmns = Dmns(dmns :+ dmn)

object Dmns:
  def none = Dmns(Nil)

case class Dmn(ident: Ident) :
  lazy val path = ident.toOriginal() + ".dmn"

