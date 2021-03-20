package camundala.model

case class BpmnsConfig(bpmns: Bpmns,
                      groups: BpmnGroups,
                      users: BpmnUsers = BpmnUsers.none
                     ) extends HasStringify :                    

  def stringify(intent: Int = 0) =
    val inStr = intentStr(intent)
    s"""${inStr}bpmnsConfig(
       |${bpmns.stringify(intent + 1)},
       |${groups.stringify(intent + 1)},
       |${users.stringify(intent + 1)}
       |${inStr})""".stripMargin

object BpmnsConfig :
  def none = BpmnsConfig(
    Bpmns.none,
    BpmnGroups.none,
    BpmnUsers.none
  )

case class Bpmns( bpmns: Seq[Bpmn]) extends HasStringify:

  def :+(bpmn: Bpmn): Bpmns = Bpmns(bpmns :+ bpmn)    

  def stringify(intent: Int = 0) =
    stringifyWrap(intent + 1, "bpmns", bpmns)

object Bpmns :
  def none = Bpmns(Nil)    

case class Bpmn(path: BpmnPath,
                processes: BpmnProcesses
               ) extends HasStringify:

  def stringify(intent: Int = 0) =
    val inStr = intentStr(intent)
    s"""${inStr}bpmn(${path.stringify(0)})
       |${stringifyWrap(intent + 1, ".processes", processes.processes)}""".stripMargin
