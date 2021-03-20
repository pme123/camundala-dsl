package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.camunda.bpm.model.{bpmn => camundaBpmn}
import org.camunda.bpm.model.bpmn.{instance => camunda}
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.jdk.CollectionConverters._

object FromCamundaBpmn 
  extends DSL
    with DSL.Implicits:
  
    extension (bpmn: Bpmn)
        def fromCamunda(outputPath: BpmnPath): Bpmn =
            given modelInstance: camundaBpmn.BpmnModelInstance = 
                camundaBpmn.Bpmn.readModelFromStream (this.getClass.getClassLoader.getResourceAsStream (bpmn.path) )
            
            val cProcesses: Seq[camunda.Process] = modelInstance.getModelElementsByType(classOf[camunda.Process]).asScala.toSeq
            bpmn.processes( cProcesses.map(p => p.fromCamunda()): _*)
            
//  bpmn.processes.processes.map (_.toCamunda)
// BpmnCamunda.writeModelToFile (new File (outputPath), modelInstance)

    extension (camundaProcess: camunda.Process)
        def fromCamunda()(using modelInstance: camundaBpmn.BpmnModelInstance): BpmnProcess = 
            process(createIdent(camundaProcess.getName))
              .elements(
                  modelInstance.getModelElementsByType(classOf[camunda.ServiceTask]).asScala.toSeq
                    .map(_.fromCamunda()) ++
                modelInstance.getModelElementsByType(classOf[camunda.UserTask]).asScala.toSeq
                  .map(_.fromCamunda())
              : _*)

    extension (camundaServiceTask: camunda.ServiceTask)
        def fromCamunda()(using modelInstance: camundaBpmn.BpmnModelInstance): ServiceTask =
            serviceTask(createIdent(camundaServiceTask.getName))

    extension (camundaUserTask: camunda.UserTask)
        def fromCamunda()(using modelInstance: camundaBpmn.BpmnModelInstance): UserTask =
            userTask(createIdent(camundaUserTask.getName))
                
    def createIdent(name: String) =
        println(s"NAME: $name")
        Ident(name.split(" ").map(_.capitalize).mkString)