package camundala
package dev

import camundala.model.*
import camundala.dev.Print.PrintObject
import zio.ZIO
import zio.console.*
import camundala.dsl.DSL

trait DslPrinter:

  import Print.*

  extension (runnerConfig: RunnerConfig)
    def print(): Print =
      val bpmnsConfig = runnerConfig.bpmnsConfig
      val projectName = runnerConfig.projectName

      pa2(
        pl("import camundala.dev.*"),
        pl("import camundala.dsl.DSL"),
        pl("import java.io.File"),
        pl("import camundala.model.BpmnsConfig\n"),
        pl(s"object ${projectName}RunnerApp extends zio.App, DSL:\n"),
        pa2(
          pl("def run(args: List[String]) ="),
          pl("    runnerLogic.exitCode\n"),
          pl(s"import $projectName._\n"),
          pl("private lazy val runnerLogic ="),
          po(
            pl("BpmnRunner("),
            po(
              pl("RunnerConfig("),
              pa(
                pl(s""""$projectName""""),
                pl("path(cawemoFolder)"),
                pl("path(withIdFolder)"),
                pl("path(generatedFolder)"),
                pl("config")
              ),
              pl(")")
            ),
            pl(").run()")
          )
        ),
        pl(s"end ${projectName}RunnerApp"),
        pl(s"object $projectName extends DSL:"),
        pa2(
          pl(s"""final val cawemoFolder = "${runnerConfig.cawemoFolder}""""),
          pl(s"""final val withIdFolder = "${runnerConfig.withIdFolder}""""),
          pl(
            s"""final val generatedFolder = "${runnerConfig.generatedFolder}""""
          ),
          po(
            pl("val config = bpmnsConfig"),
            bpmnsConfig.users.print(),
            bpmnsConfig.groups.print(),
            bpmnsConfig.bpmns.print()
          ),
          bpmnsConfig.users.printObjects(),
          bpmnsConfig.groups.printObjects(),
          bpmnsConfig.bpmns.printObjects()
        ),
        pl(s"""|end $projectName
               |""".stripMargin)
      )

  end extension

  extension (users: BpmnUsers)
    def print(): Print =
      po(
        pl(".users("),
        pa(users.users.map(_.print())),
        pl(")")
      )
    def printObjects(): Print =
      poo("users", users.users.map(_.printObjects()))
  end extension

  extension (user: BpmnUser)
    def print(): Print =
      pl(s"users.${user.username}")

    def printObjects(): Print =
      po(
        pl(s"""val ${user.username} = user("${user.username}")"""),
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
    def printObjects(): Print =
      poo("groups", groups.groups.map(_.printObjects()))
  end extension

  extension (group: BpmnGroup)
    def print(): Print =
      pl(s"groups.${group.ident}")

    def printObjects(): Print =
      po(
        pl(s"""val ${group.ident} = group("${group.ident}")"""),
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

    def printObjects(): Print =
      poo(
        "bpmns",
        bpmns.bpmns.map(_.printObjects())
      )
  end extension

  extension (bpmn: Bpmn)
    def print(): Print =
      pl(s"bpmns.${bpmn.ident}")

    def printObjects(): Print =
      pa2(
        po(
          pl(s"""val ${bpmn.ident} = bpmn("${bpmn.ident}")"""),
          po(
            bpmn.processes.print()
          )
        ),
        bpmn.processes.printObjects()
      )
  end extension

  extension (processes: BpmnProcesses)
    def print(): Print =
      po(
        pl(".processes("),
        pa(processes.processes.map(_.print())),
        pl(")")
      )
    def printObjects(): Print =
      poo(
        "processes",
        processes.processes.map(p => p.printObjects())
      )

  end extension

  extension (process: BpmnProcess)
    def print(): Print =
      pl(s"processes.${process.ident}")

    def printObjects(): Print =
      pa2(
        po(
          pl(s"""val ${process.ident} = process("${process.ident}")"""),
          process.starterGroups.print(),
          process.starterUsers.print(),
          process.nodes.print(),
          process.flows.print(),
          pa2(
            process.nodes.printObjects() :+
              process.flows.printObjects()
          )
        )
      )

  end extension

  extension (candidateGroups: CandidateGroups)
    def print(): Print =
      po(
        pl(".starterGroups("),
        pa(candidateGroups.groups.map(g => pl(s"groups.$g.ref"))),
        pl(")")
      )
  end extension

  extension (candidateUsers: CandidateUsers)
    def print(): Print =
      po(
        pl(".starterUsers("),
        pa(candidateUsers.users.map(u => pl(s"users.$u.ref"))),
        pl(")")
      )
  end extension

  extension (nodes: ProcessNodes)
    def print(): Print =
      po(
        pl(".nodes("),
        pa(nodes.elements.map(_.print())),
        pl(")")
      )
    def printObjects(): Seq[Print] =
      nodes.elements
        .groupBy(_.elemKey)
        .map { case (k, elems) =>
          poo(
            k.toString,
            elems.flatMap(_.printObjects())
          )
        }
        .toSeq

  end extension

  extension (flows: SequenceFlows)
    def print(): Print =
      po(
        pl(".flows("),
        pa(flows.elements.map(_.print())),
        pl(")")
      )
    def printObjects(): Print =
      poo(
        "flows",
        flows.elements.flatMap(_.printObjects())
      )
  end extension

  extension (elem: HasProcessElement[_])
    def print(): Print =
      pl(s"""${elem.elemKey.toString}.${elem.ident}""")

    def printObjects(): Seq[Print] =
      Seq(
        pl(s"""val ${elem.ident}Ident = "${elem.ident}""""),
        pl(
          s"""lazy val ${elem.ident} = ${elem.elemKey.name}(${elem.ident}Ident)"""
        )
      )
  end extension

  def po(pr: Print, prints: Print*) = PrintObject(pr +: prints)

  def po(prints: Seq[Print]) = PrintObject(prints)

  def poo(objectName: String, prints: Seq[Print]): Print =
    po(
      pl(s"object $objectName :\n"),
      pa2(
        if (prints.nonEmpty) (prints)
        else
          Seq(
            pl(
              s"""println("//TODO Add $objectName here or remove this object")"""
            )
          )
      ),
      pl(s"end $objectName")
    )

  def poo(objectName: String, print: Print, prints: Print*): Print =
    poo(objectName, print +: prints)

  def pa(prints: Seq[Print]) = PrintArray(prints)

  def pa(print: Print, prints: Print*) = PrintArray(print +: prints)

  def pa2(prints: Seq[Print]) = PrintArray(prints, "\n")

  def pa2(print: Print, prints: Print*) =
    PrintArray(print +: prints, "\n")

  def pl(text: String) = PrintLine(text)

end DslPrinter

case class DslPrinterRunner(runnerConfig: RunnerConfig) extends DslPrinter, DSL:

  def run(): ZIO[zio.console.Console, DslPrinterException, Seq[Bpmn]] =
    (for {
      _ <- putStrLn(
        s"Start DSL Printer"
      )
      bpmns <- FromCamundaRunner(
        FromCamundaConfig(runnerConfig.cawemoFolder, runnerConfig.withIdFolder)
      ).run()
      newRunnerConfig = runnerConfig.copy(bpmnsConfig =
        bpmnsConfig.bpmns(bpmns)
      )
      _ <- putStrLn(s"/* ${"*" * 40} */")
      _ <- putStrLn(newRunnerConfig.print().asString(0))
      _ <- putStrLn(s"/* ${"*" * 40} */")
      _ <- putStrLn(
        s"DSL Printed - copy content above to Scala file"
      )
    } yield (bpmns))
      .mapError { case t: Throwable =>
        t.printStackTrace
        DslPrinterException(t.getMessage)
      }

end DslPrinterRunner

case class DslPrinterException(msg: String)

sealed trait Print:
  def nonEmpty: Boolean
  def asString(intent: Int = -2): String

object Print:

  case class PrintObject(prints: Seq[Print]) extends Print:
    def nonEmpty = prints.nonEmpty
    def asString(intent: Int): String =
      prints
        .filter(_.nonEmpty)
        .map(_.asString(intent + 1))
        .mkString("\n")

  case class PrintArray(prints: Seq[Print], separator: String = ",")
      extends Print:
    def nonEmpty = prints.nonEmpty
    def asString(intent: Int): String =
      prints
        .filter(_.nonEmpty)
        .map(_.asString(intent + 1))
        .mkString(s"$separator\n")

  case class PrintLine(text: String) extends Print:

    def nonEmpty = text.trim.nonEmpty
    def asString(intent: Int): String =
      s"${" " * 2 * intent}$text"

  end PrintLine

end Print
