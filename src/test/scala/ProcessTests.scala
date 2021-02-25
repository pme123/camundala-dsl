import Main.getClass
import camundala.model.TaskImplementation._
import camundala.model._
import org.camunda.bpm.model.bpmn.{Bpmn => BpmnCamunda}
import org.camunda.bpm.model.bpmn.{instance => camunda}
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Test
import org.junit.Assert._
import camundala.bpmn._

import java.io.File
import scala.jdk.CollectionConverters._

class ProcessTests:

  @Test def loadProcess(): Unit =
    val fooVar: ProcessVarString = ProcessVarString("fooVar")
/*
    val bpmn = Bpmn("process.bpmn")
      .processes(
        BpmnProcess("testDslProcess")
          .canStart(BpmnUser("Darth.Vader"))
          .canStart(BpmnGroup("admin"))
          .elements(
            StartEvent("startEvent")
              .form(
                textField(fooVar.ident)
                  .prop("myProp", "helothere")
              ),
            ServiceTask("serviceTask")
              .implementation(
                fooVar.expression
                  .resultVariable("foo")
              ),
            UserTask("userTaskA")
              .form(
                enumField("myField")
                  .label("MY FIELD")
                  .value("k1", "blau")
                  .value("k2", "grau")
                  .readonly
                  .required,
                textField("textField")
                  .label("hello")
                  .default("Peter")
                  .required
                  .minlength(3)
                  .maxlength(12),
                longField("numberField")
                  .label("My Number")
                  .default("10")
                  .min(3)
                  .max(12)
              ),
            UserTask("userTaskB")
              .form("MyFormKey"),
            ScriptTask("scriptTask")
              .inlineGroovy(s"println 'hello Scala world'")
              .resultVariable("scriptResult"),
            SequenceFlow("flowIsBar")
              .inlineGroovy(s"println 'hello'\n$fooVar == 'bar'"),
            // .expression(s"$${$fooVar == 'bar'}"),
            SequenceFlow("flowIsNotBar")
              .groovy("script/asdf")
            // .expression(s"$${$fooVar != 'bar'}"),

          )
      ).toCamunda("generatedBpmn.bpmn")

*/
//BpmnCamunda.writeModelToFile(new File("generatedBpmn.bpmn"), modelInstance)

    

