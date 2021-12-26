package camundala
package bpmn

import os.Path

case class Bpmns(bpmns: Seq[Bpmn]) :

  def :+(bpmn: Bpmn): Bpmns = Bpmns(bpmns :+ bpmn)

object Bpmns:
  def none = Bpmns(Nil)

case class Bpmn(path: Path,
                processes: BpmnProcesses
               ):
   def ident: String = path.last.takeWhile(_ != '.')