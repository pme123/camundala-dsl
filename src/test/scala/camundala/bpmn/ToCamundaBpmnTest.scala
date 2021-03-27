package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.TaskImplementation.*
import camundala.model.*
import org.camunda.bpm.model.bpmn.{Bpmn => BpmnCamunda, instance => camunda}
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Assert.*
import org.junit.Test

import java.io.File
import scala.jdk.CollectionConverters._
import camundala.dsl.DSL._

class ToCamundaBpmnTest
  extends DSL
    with ToCamundaBpmn :

  @Test def toCamunda(): Unit =
    val fooVar: ProcessVarString = ProcessVarString("fooVar")

    val sequenceFlowIsNotBar = sequenceFlow("flowIsNotBar")

    val bpmnModel: Bpmn =
      bpmn("process.bpmn")
        .processes(
          process("testDslProcess")
            .starterUsers(user("Darth.Vader"))
            .starterGroups(group("admin"))
            .elements(
              startEvent("startEvent")
                .form(
                  textField(fooVar.toString)
                    .label("Add some Text")
                    .defaultValue("YES WE DO THIS!")
                    .prop("myProp",
                      "hello there"))
              ,
              serviceTask("serviceTask")
                .expression("${fooVar.toString()}", "myVar")
              ,
              userTask("userTaskA")
                .form(
                  enumField("myField")
                    .label("MY FIELD")
                    .enumValue("k1", "blau")
                    .enumValue("k2", "grau")
                    .required
                  ,
                  textField("textField")
                    .label("hello")
                    .defaultValue("Peter")
                    .readonly
                    //.minlength(3)
                   // .maxlength(12)
                  ,
                  longField("numberField")
                    .label("My Number")
                    .defaultValue("10")
                    .min(3)
                    .max(12)
                )
              ,
              userTask("userTaskB")
                .form(
                  formKey("MyFormKey")
                )
              ,
              scriptTask("scriptTask")
                .inlineGroovy(s"println 'hello Scala world'")
                .resultVariable("scriptResult")
              ,
              sequenceFlow("flowIsBar")
                .inlineGroovy(
                  s"""println 'hello'
            $fooVar == 'bar'""")
              ,
              sequenceFlowIsNotBar
              ,
              exclusiveGateway("gatewayFork")
                .defaultFlow(sequenceFlowIsNotBar.ref)
            )
        )

    println(bpmnModel.stringify())
    bpmnModel.toCamunda(path("camunda-demo/src/main/resources/generatedBpmn.bpmn"))


