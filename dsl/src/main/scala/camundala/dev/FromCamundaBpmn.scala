package camundala.dev

import camundala.dsl.DSL
import camundala.model.*
import org.camunda.bpm.model.bpmn.impl.instance.SequenceFlowImpl
import org.camunda.bpm.model.bpmn.instance.FlowElement
import org.camunda.bpm.model.{bpmn => camundaBpmn}
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, instance => camunda}

import java.io._
import scala.language.implicitConversions
import scala.language.postfixOps
import zio.*
import scala.jdk.CollectionConverters._
import zio.console.*
// scala
import scala.jdk.CollectionConverters.*
import java.util.List
import java.util.ArrayList

def testList =
  println("Using a Java List in Scala")
  val javaList: java.util.List[String] = new ArrayList()
  val scalaSeq: Seq[String] = javaList.asScala.toSeq
  scalaSeq.foreach(println)
  for s <- scalaSeq do println(s)

trait FromCamundaBpmn extends DSL with DSL.Givens:

  // context function def f(using BpmnModelInstance): T
  type FromCamundable[T] = BpmnModelInstance ?=> T
  type ZIdent = IO[FromCamundaException, String]

  def fromCamunda(
      fromCamundaConfig: FromCamundaConfig
  ): IO[FromCamundaException, Seq[Bpmn]] =
    for {
      cawemoFolder <- UIO(
        new File(fromCamundaConfig.cawemoFolder)
      )
      cawemoFiles <- cawemoBpmns(cawemoFolder)

      bpmns <- ZIO.foreach(cawemoFiles) { bpmnFile =>
        fromCamunda(
          bpmnFile,
          path(s"${fromCamundaConfig.withIdFolder}/${bpmnFile.getName}")
        )
      }
    } yield bpmns

  private def cawemoBpmns(cawemoFolder: File) =
    if (cawemoFolder.isDirectory)
      ZIO.succeed(
        cawemoFolder
          .listFiles(new FilenameFilter {
            def accept(dir: File, name: String): Boolean =
              name.endsWith(".bpmn")
          })
          .toSeq
      )
    else
      ZIO.fail(
        FromCamundaException(
          s"The configured Cawemo Folder must be a Directory (${cawemoFolder.getAbsolutePath} is not!)."
        )
      )

  private def fromCamunda(
      bpmnFile: File,
      outputPath: BpmnPath
  ): IO[FromCamundaException, Bpmn] = {
    (for {
      modelInstance <- ZIO(
        camundaBpmn.Bpmn.readModelFromFile(bpmnFile)
      )
      cProcesses <- ZIO(
        modelInstance
          .getModelElementsByType(classOf[camunda.Process])
          .asScala
          .toSeq
      )
      processes <- ZIO.collect(cProcesses) { p =>
        p.fromCamunda()(using modelInstance)
          .mapError(Some(_))
      }
      _ <- ZIO(
        camundaBpmn.Bpmn.writeModelToFile(new File(outputPath), modelInstance)
      )
    } yield bpmn(bpmnFile).processes(processes: _*))
      .mapError {
        case Some(ex: FromCamundaException) => ex
        case t: Throwable =>
          t.printStackTrace
          FromCamundaException(t.getMessage)
      }
  }

  extension (camundaProcess: camunda.Process)
    def fromCamunda(): FromCamundable[IO[FromCamundaException, BpmnProcess]] =
      for {
        ident <- camundaProcess.createIdent()
        startEvents <- createElements(classOf[camunda.StartEvent], startEvent)
        userTasks <- createElements(classOf[camunda.UserTask], userTask)
        serviceTasks <- createElements(
          classOf[camunda.ServiceTask],
          serviceTask
        )
        scriptTasks <- createElements(classOf[camunda.ScriptTask], scriptTask)
        businessRuleTasks <- createElements(
          classOf[camunda.BusinessRuleTask],
          businessRuleTask
        )
        exclusiveGateways <- createElements(
          classOf[camunda.ExclusiveGateway],
          exclusiveGateway
        )
        parallelGateways <- createElements(
          classOf[camunda.ParallelGateway],
          parallelGateway
        )
        endEvents <- createElements(classOf[camunda.EndEvent], endEvent)
        sequenceFlows <- createElements(
          classOf[camunda.SequenceFlow],
          sequenceFlow
        )
      } yield process(ident)
        .nodes(
          startEvents ++
            userTasks ++
            serviceTasks ++
            scriptTasks ++
            businessRuleTasks ++
            exclusiveGateways ++
            parallelGateways ++
            endEvents: _*
        )
        .flows(sequenceFlows: _*)

  def createElements[T <: camunda.FlowElement, C](
      clazz: Class[T],
      constructor: String => C
  ): FromCamundable[IO[FromCamundaException, Seq[C]]] = {
    ZIO.collect(
      summon[BpmnModelInstance].getModelElementsByType(clazz).asScala.toSeq
    ) { fe =>
      val zident: ZIdent = fe.createIdent()
      zident
        .mapError(e => Some(e))
        .map(ident => constructor(ident))
    }
  }

  extension (process: camunda.Process)
    def createIdent(): ZIdent =
      for {
        ident <- identString(Option(process.getName), process)
        _ = process.setId(ident)
      } yield ident

  extension (element: camunda.FlowElement)
    def generateIdent(): ZIdent =
      identString(Option(element.getName), element)

    def createIdent(): ZIdent =
      for {
        ident <-
          element match
            case flow: SequenceFlowImpl =>
              flow.createIdent()
            case _ =>
              generateIdent()
        _ = element.setId(ident)
      } yield ident

  extension (element: camunda.SequenceFlow)
    def createIdent(): ZIdent = {
      for {
        ident <- element.generateIdent()
        sourceIdent <- element.getSource.generateIdent()
        targetIdent <- element.getTarget.generateIdent()
        newIdent = s"${ident}__${sourceIdent}__${targetIdent}"
        _ = element.setId(newIdent)
      } yield newIdent
    }

  def identString(name: Option[String], camObj: camunda.BaseElement): ZIdent =
    val elemKey = camObj.getElementType.getTypeName
    zio
      .Task(
        name match
          case Some(n) =>
            n.split(" ")
              .map(_.capitalize)
              .mkString
              .replaceAll("[^a-zA-Z0-9]", "")
          case None =>
            camObj.getId
      )
      .mapError(ex =>
        ex.printStackTrace
        FromCamundaException(s"Could not create an Ident for $elemKey / $name")
        )

end FromCamundaBpmn

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
