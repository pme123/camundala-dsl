package camundala
package dev

trait CompareBpmns extends DSL:

  import AuditEntry.*

  extension (bpmnsConfig: BpmnsConfig)
    def compareWith(newBpmns: Seq[Bpmn]): CompareAudit =
      CompareAudit(
        bpmnsConfig.bpmns.bpmns.flatMap { bpmn =>
         newBpmns
            .find(_.ident == bpmn.ident)
            .map(newBpmn =>
              info(s"BPMN ident match (${bpmn.ident}).") +: bpmn.processes
                .compareWith(newBpmn.processes)
            )
            .getOrElse(
              Seq(
                warn(
                  s"BPMN ident has changed: ${bpmn.ident} -> new Bpmns: ${newBpmns.map(_.ident).mkString(", ")}."
                )
              )
            )
        }
      )

  extension (processes: BpmnProcesses)
    def compareWith(newProcesses: BpmnProcesses): Seq[AuditEntry] =
      processes.processes.collect {
        case proc if newProcesses.processes.exists(_.ident == proc.ident) =>
          proc.compareWith(
            newProcesses.processes.filter(_.ident == proc.ident).head
          )
        case proc =>
          Seq(
            warn(
              s"There is no Process with id '${proc.ident}' in the new BPMN."
            )
          )
      }.flatten ++
        newProcesses.processes.collect {
          case newProc
              if !processes.processes.exists(_.ident == newProc.ident) =>
            warn(
              s"There is no Process with id '${newProc.ident}' in the existing BPMN."
            )
        }

  extension (process: BpmnProcess)
    def compareWith(newProcess: BpmnProcess): Seq[AuditEntry] = Seq(
      info(s"Process '${process.ident}' exists.")
    ) ++
      process.nodes.compareWith(newProcess.nodes) ++
      process.flows.compareWith(newProcess.flows)

  extension (elements: ProcessElements)
    def compareWith(newElements: ProcessElements): Seq[AuditEntry] =
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

case class CompareAudit(entries: Seq[AuditEntry]):
  def maxLevel() =
    entries
      .map(_.level)
      .sortBy(_.rank)
      .headOption
      .getOrElse(AuditLevel.INFO)

  def print() =
    println(log())

  def log(auditLevel: AuditLevel = AuditLevel.INFO) =
    (
      Seq("** Compare Audit Log:     **") ++
        entries
          .sortBy(_.level.rank)
          .filter(_.level.rank <= auditLevel.rank)
          .map(_.log()) ++
        Seq("** End Compare Audit Log: **")
    ).mkString("\n")

end CompareAudit

case class AuditEntry(level: AuditLevel, msg: String):
  def log() =
    String.format("%-5s|", level.toString) + msg

object AuditEntry:
  def info(msg: String) = AuditEntry(AuditLevel.INFO, msg)

  def warn(msg: String) = AuditEntry(AuditLevel.WARN, msg)

  def error(msg: String) = AuditEntry(AuditLevel.ERROR, msg)

enum AuditLevel(val rank: Int):

  case INFO extends AuditLevel(3)

  case WARN extends AuditLevel(2)

  case ERROR extends AuditLevel(1)
