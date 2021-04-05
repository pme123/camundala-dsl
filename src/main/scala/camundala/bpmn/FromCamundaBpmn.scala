package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.camunda.bpm.model.bpmn.impl.instance.SequenceFlowImpl
import org.camunda.bpm.model.bpmn.instance.FlowElement
import org.camunda.bpm.model.{bpmn => camundaBpmn}
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, instance => camunda}

import java.io.File
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.jdk.CollectionConverters.*
import zio.*

trait FromCamundaBpmn
  extends DSL
    with DSL.Givens :

  // context function def f(using BpmnModelInstance): T
  type FromCamundable[T] = BpmnModelInstance ?=> T
  type ZIdent = IO[FromCamundaException, String]

  def fromCamunda(bpmnPath: BpmnPath, outputPath: BpmnPath): IO[FromCamundaException, Bpmn] =
    bpmn(bpmnPath)
      .fromCamunda(outputPath)

  extension (_bpmn: Bpmn)
    def fromCamunda(outputPath: BpmnPath): IO[FromCamundaException, Bpmn] = {
      (for {
        modelInstance <- ZIO(camundaBpmn.Bpmn.readModelFromFile(new File(_bpmn.path)))
        cProcesses <- ZIO(modelInstance.getModelElementsByType(classOf[camunda.Process]).asScala.toSeq)
        processes <- ZIO.collect(cProcesses) {
          p =>
            p.fromCamunda()(using modelInstance)
              .mapError(Some(_))
        }
        _ <- ZIO(camundaBpmn.Bpmn.writeModelToFile(new File(outputPath), modelInstance))
      } yield bpmn(outputPath.toString).processes(processes: _*))
        .mapError {
          case Some(ex: FromCamundaException) => ex
          case t: Throwable => FromCamundaException(t.getMessage)
        }
    }

  extension (camundaProcess: camunda.Process)
    def fromCamunda(): FromCamundable[IO[FromCamundaException, BpmnProcess]] =
      for {
        ident <- camundaProcess.createIdent()
        startEvents <- createElements(classOf[camunda.StartEvent], startEvent)
        userTasks <- createElements(classOf[camunda.UserTask], userTask)
        scriptTasks <- createElements(classOf[camunda.ScriptTask], scriptTask)
        businessRuleTasks <- createElements(classOf[camunda.BusinessRuleTask], businessRuleTask)
        exclusiveGateways <- createElements(classOf[camunda.ExclusiveGateway], exclusiveGateway)
        parallelGateways <- createElements(classOf[camunda.ParallelGateway], parallelGateway)
        endEvents <- createElements(classOf[camunda.EndEvent], endEvent)
        sequenceFlows <- createElements(classOf[camunda.SequenceFlow], sequenceFlow)
      } yield
        process(ident)
          .elements(
            startEvents ++
              userTasks ++
              scriptTasks ++
              businessRuleTasks ++
              exclusiveGateways ++
              parallelGateways ++
              endEvents ++
              sequenceFlows
              : _*)

  def createElements[T <: camunda.FlowElement](clazz: Class[T], element: String => ProcessElement)
  : FromCamundable[IO[FromCamundaException, Seq[ProcessElement]]] = {
    ZIO.collect(summon[BpmnModelInstance].getModelElementsByType(clazz).asScala.toSeq) {
      fe =>
        val zident: ZIdent = fe.createIdent()
        zident
          .mapError(e => Some(e))
          .map(ident => element(ident))
    }
  }

  extension (process: camunda.Process)
    def createIdent(): ZIdent = 
      for{
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
        newIdent =  s"${ident}_${sourceIdent}-${targetIdent}"
        _ = element.setId(newIdent)
      } yield
       newIdent
    }

  def identString(name: Option[String], camObj: camunda.BaseElement): ZIdent =
    val elemType = camObj.getElementType.getTypeName
    zio.Task(
      name match
        case Some(n) =>
          n.split(" ").map(_.capitalize).mkString
        case None =>
          camObj.getId
    ).mapError(ex => FromCamundaException(s"Could not create an Ident for $elemType / $name"))

case class FromCamundaException(msg: String)