package camundala
package camunda

import bpmn.*
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn as CBpmn}
import os.RelPath
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import java.io.File
import scala.language.implicitConversions

trait InitCamundaBpmn extends BpmnDsl, ProjectPaths, App:

  def run(name: String): Unit =
    val bpmns: Seq[(String, Seq[Process[?, ?]])] =
      for cawemoFile <- cawemoBpmns(cawemoPath.toIO)
      yield
        println(s"// CAWEMO: $cawemoFile")
        Path(cawemoFile)
        val processes = fromCamunda(
          cawemoFile
        )

        cawemoFile.getName -> processes
    printGenerator(name, bpmns)

  private def cawemoBpmns(cawemoFolder: File): Seq[File] =
    if (cawemoFolder.isDirectory)
      cawemoFolder
        .listFiles(new FilenameFilter {
          def accept(dir: File, name: String): Boolean =
            name.endsWith(".bpmn")
        })
        .toSeq
    else
      throw IllegalArgumentException(
        s"The cawemoFolder must be a directory! -> $cawemoFolder"
      )

  private def fromCamunda(
      cawemoFile: File
  ) =
    implicit val modelInstance: BpmnModelInstance =
      CBpmn.readModelFromFile(cawemoFile)
    val cProcesses = modelInstance
      .getModelElementsByType(classOf[CProcess])
      .asScala
      .toSeq
    CBpmn.writeModelToFile(
      (withIdPath / cawemoFile.getName).toIO,
      modelInstance
    )
    cProcesses.map(_.fromCamunda())

  extension (camundaProcess: CProcess)
    def fromCamunda(): FromCamundable[Process[?, ?]] =
      val ident = camundaProcess.createIdent()
      process(ident).copy(elements =
        createElements(classOf[CUserTask], UserTask.init) ++
          createElements(classOf[CServiceTask], ServiceTask.init) ++
          createElements(classOf[CBusinessRuleTask], DecisionDmn.init) ++
          createElements(classOf[CEndEvent], EndEvent.init)
      )

  private def createElements[
      T <: CFlowElement,
      C,
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      clazz: Class[T],
      constructor: String => ProcessElement[In, Out, ?]
  ): FromCamundable[Seq[ProcessElement[In, Out, ?]]] = {
    val elems = summon[CBpmnModelInstance]
      .getModelElementsByType(clazz)
      .asScala
      .toSeq
    elems.map { fe =>
      constructor(fe.createIdent())
    }
  }

  extension (process: CProcess)
    def createIdent(): String =
      val ident = identString(Option(process.getName), process)
      process.setId(ident)
      ident

  extension (element: CFlowElement)
    def generateIdent(): String =
      identString(Option(element.getName), element)

    def createIdent(): String =
      val ident: String =
        element match
          case flow: CSequenceFlow =>
            flow.createIdent()
          case _ =>
            generateIdent()
      element.setId(ident)
      ident

  extension (element: CSequenceFlow)
    def createIdent() =
      val ident = element.generateIdent()
      val sourceIdent = element.getSource.generateIdent()
      val targetIdent = element.getTarget.generateIdent()
      val newIdent = s"${ident}__${sourceIdent}__${targetIdent}"
      element.setId(newIdent)
      newIdent

  def identString(name: Option[String], camObj: CBaseElement): String =
    val elemKey: String =
      camObj.getElementType.getTypeName.capitalize.filter(c =>
        s"$c".matches("[A-Z]")
      )

    name match
      case Some(n) =>
        n.split(" ")
          .map(_.capitalize)
          .mkString
          .replaceAll("[^a-zA-Z0-9]", "") + elemKey
      case None =>
        camObj.getId

  private def printGenerator(
      name: String,
      bpmns: Seq[(String, Seq[Process[?, ?]])]
  ): Unit =
    println(s"""
import bpmn.*
import domain.*
import camunda.GenerateCamundaBpmn
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object ${name}GenerateCamundaBpmnApp extends GenerateCamundaBpmn:

  val projectPath = pwd / ${projectPath
      .relativeTo(pwd)
      .segments
      .map(s => s"\"$s\"")
      .mkString(" / ")}
  import ${name}Domain.*
  run(${bpmns
      .map { case (fileName, procs) =>
        s"""Bpmn(withIdPath / "$fileName", ${procs
          .map(_.id)
          .mkString(", ")})"""
      }
      .mkString(",\n         ")})

end ${name}GenerateCamundaBpmnApp
object ${name}Domain extends PureDsl:

${bpmns
      .map(bpmn =>
        s"  // ${bpmn._1}\n" +
          bpmn._2
            .map { p =>
              print(p) +
                p.elements.map(print).mkString("\n", "\n", "")
            }
            .mkString("\n")
      )
      .mkString("\n")}

end ${name}Domain
""")
  private def print(inOut: InOut[?, ?, ?]): String =
    s"""  val ${inOut.id}Ident ="${inOut.id}"
       |  lazy val ${inOut.id} = ${inOut.label}(
       |    ${inOut.id}Ident,
       |    ${if (inOut.hasInOut)
      """in = NoInput(),
       |    out = NoOutput()"""
    else ""}
       |    descr = None
       |  )
       |""".stripMargin
end InitCamundaBpmn

/*
case class FromCamundaRunner(fromCamundaConfig: FromCamundaConfig)
    extends FromCamundaBpmn:

  def run(): ZIO[zio.console.Console, FromCamundaException, Seq[Bpmn]] =
    (for {
      _: Any <- putStrLn(
        s"Start From Camunda BPMNs from ${fromCamundaConfig.cawemoFolder}"
      )
      bpmns: Seq[Bpmn] <- fromCamunda(fromCamundaConfig)
      _: Any <- putStrLn(
        s"Generated BPMNs to ${fromCamundaConfig.withIdFolder}"
      )
    } yield (bpmns))
      .mapError { case t: Throwable =>
        t.printStackTrace
        FromCamundaException(t.getMessage)
      }

end FromCamundaRunner

case class FromCamundaConfig(
    cawemoFolder: BpmnPath,
    withIdFolder: BpmnPath
)

case class FromCamundaException(msg: String)
 */
