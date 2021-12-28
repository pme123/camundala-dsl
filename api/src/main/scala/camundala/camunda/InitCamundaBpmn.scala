package camundala
package camunda

import camundala.bpmn.PureDsl
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn as CBpmn}

import java.io.File
import scala.language.implicitConversions

trait InitCamundaBpmn extends PureDsl, App:

  // context function def f(using BpmnModelInstance): T
  type FromCamundable[T] = CBpmnModelInstance ?=> T
  def projectPath: Path
  def cawemoPath: Path = projectPath / "cawemo"
  def withIdPath: Path = cawemoPath / "with-ids"

  def run(): Unit =
    for cawemoFile <- cawemoBpmns(cawemoPath.toIO)
    yield
      println(s"// CAWEMO: $cawemoFile")
      val modelInstance = fromCamunda(
        Path(cawemoFile)
      )
      println(s"// WITH IDS: ${(withIdPath / cawemoFile.getName).toIO}")
      CBpmn.writeModelToFile(
        (withIdPath / cawemoFile.getName).toIO,
        modelInstance
      )

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
      bpmnPath: Path
  ): BpmnModelInstance =
    implicit val modelInstance: BpmnModelInstance =
      CBpmn.readModelFromFile(bpmnPath.toIO)
    val v = modelInstance
      .getModelElementsByType(classOf[CProcess])
      .asScala
      .toSeq

    v.foreach(_.fromCamunda())
    modelInstance
  /*

  private def fromCamunda(
      bpmnFile: File,
      outputPath: BpmnPath
  ): IO[FromCamundaException, Bpmn] = {
    (for {
      modelInstance <- ZIO(
        CBpmn.readModelFromFile(bpmnFile)
      )
      cProcesses <- ZIO(
        modelInstance
          .getModelElementsByType(classOf[CProcess])
          .asScala
          .toSeq
      )
      processes <- ZIO.collect(cProcesses) { p =>
        p.fromCamunda()(using modelInstance)
          .mapError(Some(_))
      }
      _ <- ZIO(
        CBpmn.writeModelToFile(new File(outputPath), modelInstance)
      )
    } yield bpmn(bpmnFile).processes(processes: _*))
      .mapError {
        case Some(ex: FromCamundaException) => ex
        case t: Throwable =>
          t.printStackTrace
          FromCamundaException(t.getMessage)
      }
  }
   */
  extension (camundaProcess: CProcess)
    def fromCamunda(): FromCamundable[Unit] =
      val ident = camundaProcess.createIdent()
      printInOut(ident)
      createElements(classOf[CUserTask], "userTask")
      createElements(classOf[CServiceTask], "serviceTask")
      createElements(classOf[CBusinessRuleTask], "dmn")

  private def printInOut(ident: String): Unit =
    println(s"""
               |  val ${ident}Ident ="${ident}Ident"
               |  lazy val $ident = process(
               |    ${ident}Ident,
               |    in = NoInput(),
               |    out = NoOutput(),
               |    descr = None
               |  )
               |""".stripMargin)

  /*     for {
          ident <- camundaProcess.createIdent()
          startEvents <- createElements(classOf[CStartEvent], startEvent)
          userTasks <- createElements(classOf[CUserTask], userTask)
          serviceTasks <- createElements(
            classOf[CServiceTask],
            serviceTask
          )
          scriptTasks <- createElements(classOf[CScriptTask], scriptTask)
          callActivities <- createElements(classOf[CCallActivity], callActivity)
          businessRuleTasks <- createElements(
            classOf[CBusinessRuleTask],
            businessRuleTask
          )
          exclusiveGateways <- createElements(
            classOf[CExclusiveGateway],
            exclusiveGateway
          )
          parallelGateways <- createElements(
            classOf[CParallelGateway],
            parallelGateway
          )
          endEvents <- createElements(classOf[CEndEvent], endEvent)
          sequenceFlows <- createElements(
            classOf[CSequenceFlow],
            sequenceFlow
          )
        } yield process(ident)
          .nodes(
            startEvents ++
              userTasks ++
              serviceTasks ++
              scriptTasks ++
              callActivities ++
              businessRuleTasks ++
              exclusiveGateways ++
              parallelGateways ++
              endEvents: _*
          )
          .flows(sequenceFlows: _*)
   */
  private def createElements[T <: CFlowElement, C](
      clazz: Class[T],
      constructor: String
  ): FromCamundable[Unit] = {
    val elems = summon[CBpmnModelInstance]
      .getModelElementsByType(clazz)
      .asScala
      .toSeq
    elems.foreach { fe =>
      fe.createIdent()
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

    def createIdent(): Unit =
      val ident: String =
        element match
          case flow: CSequenceFlow =>
            flow.createIdent()
          case _ =>
            generateIdent()
      element.setId(ident)
      printInOut(ident)

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
