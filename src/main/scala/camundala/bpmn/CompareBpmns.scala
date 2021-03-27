package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*

trait CompareBpmns
  extends DSL :

  import AuditEntry.*

  extension (bpmn: Bpmn)
    def mergeWith(newBpmn: Bpmn): MergeAudit =
      MergeAudit(
        Seq((bpmn.path == newBpmn.path) match
          case true => info(s"BPMN path match (${bpmn.path}).")
          case false => warn(s"BPMN path has changed: ${bpmn.path} -> new: ${newBpmn.path}.")) ++
          bpmn.processes.mergeWith(newBpmn.processes)
      )

  extension (processes: BpmnProcesses)
    def mergeWith(newProcesses: BpmnProcesses): Seq[AuditEntry] =
      processes.processes.collect {
        case proc if newProcesses.processes.exists(_.ident == proc.ident) =>
          proc.mergeWith(newProcesses.processes.filter(_.ident == proc.ident).head)
        case proc =>
          Seq(warn(s"There is no Process with id '${proc.ident}' in the new BPMN."))
      }.flatten ++
        newProcesses.processes.collect {
          case newProc if !processes.processes.exists(_.ident == newProc.ident) =>
            warn(s"There is no Process with id '${newProc.ident}' in the existing BPMN.")
        }

  extension (process: BpmnProcess)
    def mergeWith(newProcess: BpmnProcess): Seq[AuditEntry] = Seq(
      info(s"Process '${process.ident}' exists.")
    ) ++
      process.elements.mergeWith(newProcess.elements)
  
  extension (elements: ProcessElements)
    def mergeWith(newElements: ProcessElements): Seq[AuditEntry] = 
      elements.elements.collect {
        case elem if newElements.elements.exists(_.ident == elem.ident) =>
          info(s"'${elem.ident}' exists.")
        case elem =>
          warn(s"There is no '${elem.ident}' in the new BPMN.")
      } ++
      newElements.elements.collect {
        case newElem if !elements.elements.exists(_.ident == newElem.ident) =>
          warn(s"There is no '${newElem.ident}' in the existing BPMN.")
      }
  
end CompareBpmns

case class MergeAudit(entries: Seq[AuditEntry]):
  def maxLevel() =
    entries
      .map(_.level)
      .sortBy(_.rank)
      .headOption
      .getOrElse(AuditLevel.INFO)

  def print() =
    println("** Merging Audit Log:     **")
    entries.sortBy(_.level.rank).map(_.print())
    println("** End Merging Audit Log: **")

end MergeAudit

case class AuditEntry(level: AuditLevel, msg: String):
  def print() =
    println(String.format("%-5s|", level.toString) + msg)

object AuditEntry:
  def info(msg: String) = AuditEntry(AuditLevel.INFO, msg)

  def warn(msg: String) = AuditEntry(AuditLevel.WARN, msg)

  def error(msg: String) = AuditEntry(AuditLevel.ERROR, msg)

enum AuditLevel(val rank: Int):

  case INFO extends AuditLevel(3)

  case WARN extends AuditLevel(2)

  case ERROR extends AuditLevel(1)