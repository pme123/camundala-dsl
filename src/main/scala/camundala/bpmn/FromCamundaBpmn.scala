package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.camunda.bpm.model.bpmn.impl.instance.SequenceFlowImpl
import org.camunda.bpm.model.bpmn.instance.FlowElement
import org.camunda.bpm.model.{bpmn => camundaBpmn}
import org.camunda.bpm.model.bpmn.{instance => camunda}

import java.io.File
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.jdk.CollectionConverters._

trait FromCamundaBpmn
  extends DSL
    with DSL.Implicits :
  
  def fromCamunda(bpmnPath: BpmnPath, outputPath: BpmnPath): Bpmn = 
    bpmn(bpmnPath)
      .fromCamunda(outputPath)

  extension (bpmn: Bpmn)
    def fromCamunda(outputPath: BpmnPath): Bpmn =
      given modelInstance:camundaBpmn.BpmnModelInstance =
        camundaBpmn.Bpmn.readModelFromStream (this.getClass.getClassLoader.getResourceAsStream (bpmn.path) )
  
      val cProcesses: Seq[camunda.Process] = modelInstance.getModelElementsByType (classOf[camunda.Process] ).asScala.toSeq
      val bpmnModel = bpmn.processes (cProcesses.map (p => p.fromCamunda () ):
      _*)
      camundaBpmn.Bpmn.writeModelToFile (new File(outputPath), modelInstance)
      bpmnModel
  
  extension (camundaProcess: camunda.Process)
    def fromCamunda()(using modelInstance: camundaBpmn.BpmnModelInstance): BpmnProcess =
      process(camundaProcess.createIdent())
        .elements(
          createElements(classOf[camunda.ServiceTask], serviceTask) ++
            createElements(classOf[camunda.UserTask], userTask) ++
            createElements(classOf[camunda.ScriptTask], scriptTask) ++
            createElements(classOf[camunda.BusinessRuleTask], businessRuleTask) ++
            createElements(classOf[camunda.ExclusiveGateway], exclusiveGateway) ++
            createElements(classOf[camunda.ParallelGateway], parallelGateway) ++
            createElements(classOf[camunda.SequenceFlow], sequenceFlow)
            : _*)

  def createElements[T <: camunda.FlowElement](clazz: Class[T], element: String => ProcessElement)
                                              (using modelInstance: camundaBpmn.BpmnModelInstance)
  : Seq[ProcessElement] =
    modelInstance.getModelElementsByType(clazz).asScala.toSeq
      .map(fe => element(fe.createIdent()))
  
  extension (process: camunda.Process)
    def createIdent(): String = identString(Option(process.getName), process)

  extension (element: camunda.FlowElement)
    def generateIdent(): String =
      identString(Option(element.getName), element)
    
    def createIdent(): String = 
      val ident = element match
        case flow:SequenceFlowImpl => 
            flow.createIdent()
        case _ =>
          generateIdent()
      element.setId(ident)
      ident
  
  extension (element: camunda.SequenceFlow)
    def createIdent(): String =
      s"${element.generateIdent()}_${element.getSource.generateIdent()}-${element.getTarget.generateIdent()}"

  def identString(name: Option[String], camObj: camunda.BaseElement): String = 
    val elemType = camObj.getElementType.getTypeName
    name match
      case Some(n) =>
            s"${elemType}_${n.split(" ").map(_.capitalize).mkString}"
      case None => 
        import java.security.MessageDigest
        import java.math.BigInteger
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(camObj.toString.getBytes)
        val bigInt = new BigInteger(1,digest)
        val hashedString = bigInt.toString(16)
        s"${elemType}_${hashedString.take(8)}"
  