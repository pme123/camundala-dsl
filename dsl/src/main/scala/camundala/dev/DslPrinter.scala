package camundala.dev

import camundala.model.*

trait DslPrinter:

  import Print.*

  extension (bpmnsConfig: BpmnsConfig)
    def print(): Print =
      po(
        pl("bpmnsConfig"),
        bpmnsConfig.users.print(),
        bpmnsConfig.groups.print(),
        bpmnsConfig.bpmns.print()
      )

  end extension

  extension (users: BpmnUsers)
    def print(): Print =
      po(
        pl(".users("),
        pa(users.users.map(_.print())),
        pl(")")
      )
  end extension

  extension (user: BpmnUser)
    def print(): Print =
      po(
        pl(s"""user("${user.username}")"""),
        po(
          user.maybeName.map(n => pl(s""".name("$n")""")).toSeq ++
            user.maybeFirstName.map(n => pl(s""".firstName("$n")""")).toSeq ++
            user.maybeEmail.map(e => pl(s""".email("$e")""")).toSeq ++
            user.groupRefs.groupRefs.map(g => pl(s"""group("$g")"""))
        )
      )
  end extension

  extension (groups: BpmnGroups)
    def print(): Print =
      po(
        pl(".groups("),
        pa(groups.groups.map(_.print())),
        pl(")")
      )
  end extension

  extension (group: BpmnGroup)
    def print(): Print =
      po(
        pl(s"""group("${group.ident}")"""),
        po(
          pl(s""".groupType("${group.`type`}")"""),
          group.maybeName.map(n => pl(s""".name("$n")""")).toSeq: _*
        )
      )
  end extension

  extension (bpmns: Bpmns)
    def print(): Print =
      po(
        pl(".bpmns("),
        pa(bpmns.bpmns.map(_.print())),
        pl(")")
      )
  end extension

  extension (bpmn: Bpmn)
    def print(): Print =
      po(
        pl(s"""bpmn("${bpmn.path}")"""),
        bpmn.processes.print()
      )
  end extension

  extension (processes: BpmnProcesses)
    def print(): Print =
      po(
        pl(".processes("),
        pa(processes.processes.map(_.print())),
        pl(")")
      )

  end extension

  extension (process: BpmnProcess)
    def print(): Print =
      po(
        pl(s"""process("${process.ident}")"""),
        process.starterGroups.print(),
        process.starterUsers.print(),
        process.nodes.print(),
        process.flows.print()
      )

  end extension

  extension (candidateGroups: CandidateGroups)
    def print(): Print =
      po(candidateGroups.groups.map(g => pl(s""".starterGroup("$g")""")))
  end extension

  extension (candidateUsers: CandidateUsers)
    def print(): Print =
      po(candidateUsers.users.map(u => pl(s""".starterUser("$u")""")))
  end extension

  extension (nodes: ProcessNodes)
    def print(): Print =
      po(
        pl(".nodes("),
          pa(nodes.elements.map(_.print())),
        pl(")")
      )
  end extension

  extension (flows: SequenceFlows)
    def print(): Print =
      po(
        pl(".flows("),
          pa(flows.elements.map(_.print())),
        pl(")")
      )
  end extension

  extension (elem:  HasProcessElement[_])
    def print(): Print =
      pl(s"""${elem.elemKey.name}("${elem.ident}")""")
  end extension

  def po(pr: Print, lines: Print*) = PrintObject(pr +: lines)

  def po(lines: Seq[Print]) = PrintObject(lines)

  def pa(lines: Seq[Print]) = PrintArray(lines)

  def pl(text: String) = PrintLine(text)

sealed trait Print:
  def nonEmpty: Boolean
  def asString(intent: Int): String

object Print:

  case class PrintObject(lines: Seq[Print]) extends Print:
    def nonEmpty = lines.nonEmpty
    def asString(intent: Int): String =
      lines
        .filter(_.nonEmpty)
        .map(_.asString(intent + 1))
        .mkString("\n")

  case class PrintArray(lines: Seq[Print]) extends Print:
    def nonEmpty = lines.nonEmpty
    def asString(intent: Int): String =
      lines
        .filter(_.nonEmpty)
        .map(_.asString(intent + 1))
        .mkString(",\n")

  case class PrintLine(text: String) extends Print:

    def nonEmpty = text.trim.nonEmpty
    def asString(intent: Int): String =
      s"${" " * 2 * intent}$text"
