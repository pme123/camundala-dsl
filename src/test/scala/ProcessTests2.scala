import Main.getClass
import camundala.bpmn._
import camundala.model.TaskImplementation._
import camundala.model._
import org.camunda.bpm.model.bpmn.{Bpmn => BpmnCamunda, instance => camunda}
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Assert._
import org.junit.Test

import java.io.File
import scala.jdk.CollectionConverters._
import camundala.dsl.DSL._

class ProcessTests2:

  @Test def loadProcess(): Unit =
    val fooVar: ProcessVarString = ProcessVarString("fooVar")

/*    val bpmnModel =
      bpmn(
        path("process.bpmn"),
        process()
      )
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

    

