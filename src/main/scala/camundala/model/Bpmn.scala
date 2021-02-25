package camundala.model

case class BpmnsConfig(bpmns: Bpmns,
                      groups: BpmnGroups,
                      users: BpmnUsers
                     ) extends HasStringify :

  def stringify(intent: Int = 0) =
    val inStr = intentStr(intent)
    s"""${inStr}bpmnsConfig(
       |${bpmns.stringify(intent + 1)},
       |${groups.stringify(intent + 1)},
       |${users.stringify(intent + 1)}
       |${inStr})""".stripMargin

case class Bpmns( bpmns: Seq[Bpmn]) extends HasStringify:

  def stringify(intent: Int = 0) =
    stringifyWrap(intent + 1, "bpmns", bpmns)
    
case class Bpmn(bpmnPath: BpmnPath,
                processes: BpmnProcesses
               ) extends HasStringify:

  def stringify(intent: Int = 0) =
    val inStr = intentStr(intent)
    s"""${inStr}bpmn(
       |${bpmnPath.stringify(intent + 1)},
       |${processes.processes.map(_.stringify(intent + 1)).mkString(",\n")}
       |${inStr})""".stripMargin

