import Main.getClass
import camundala.model.TaskImplementation._
import camundala.model._
import org.camunda.bpm.model.bpmn.{Bpmn => BpmnCamunda}
import org.camunda.bpm.model.bpmn.{instance => camunda}
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Test
import org.junit.Assert._
import camundala.bpmn.{_, given}

import camundala.model.GeneratedForm.textField

import java.io.File
import scala.jdk.CollectionConverters._

class ProcessTests:

  @Test def loadProcess(): Unit =
    val fooVar: ProcessVarString = ProcessVarString("fooVar")
    
    val bpmn = Bpmn("process.bpmn")
      .processes(
        BpmnProcess("testDslProcess")
          .canStart(BpmnUser("Darth.Vader"))
          .canStart(BpmnGroup("admin"))
          .elements(
            StartEvent("startEvent")
              .form(
                GeneratedForm()
                  .fields(
                    textField(fooVar.ident)
                  )
              ),
            ServiceTask("serviceTask")
              .implementation(
                fooVar.expression
                  .resultVariable("foo")
              ),
            UserTask("userTaskA"),
            UserTask("userTaskB"),
            ScriptTask("scriptTask")
              .inlineGroovy("println 'hello Scala world'")
              .resultVariable("scriptResult")
          )
      ).toCamunda("generatedBpmn.bpmn")
      

    //BpmnCamunda.writeModelToFile(new File("generatedBpmn.bpmn"), modelInstance)

    

