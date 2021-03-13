import Main.getClass
import camundala.bpmn.*
import camundala.dsl.DSL
import camundala.model.TaskImplementation._
import camundala.model._
import org.camunda.bpm.model.bpmn.{Bpmn => BpmnCamunda, instance => camunda}
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Assert._
import org.junit.Test

import java.io.File
import scala.jdk.CollectionConverters._
import camundala.dsl.DSL._

class ProcessTests2 extends DSL :

  @Test def loadProcess(): Unit =
    val fooVar: ProcessVarString = ProcessVarString("fooVar")

    val bpmnModel: Bpmn =
      bpmn(
        path("process.bpmn"),
        process(
          ident("testDslProcess"),
          starterUsers(user("Darth.Vader")),
          starterGroups(group("admin")),
          elements(
            startEvent(
              ident("startEvent"),
              form(
                textField(
                  ident(fooVar.toString),
                  prop(
                    ident("myProp"),
                    "hello there"))
              )
            ),
            serviceTask(
              ident("serviceTask"),
              expression("${myVar as String}", "myVar")
            ),
            userTask(
              ident("userTaskA"),
              form(
                enumField(
                  ident("myField"),
                  label("MY FIELD"),
                  enumValue(ident("k1"), name("blau")),
                  enumValue(ident("k2"), name("grau")),
                  readonly,
                  required,
                ),
                textField(
                  ident("textField"),
                  label("hello"),
                  defaultValue("Peter"),
                  required,
                  minlength(3),
                  maxlength(12)
                ),
                longField(
                  ident("numberField"),
                  label("My Number"),
                  defaultValue("10"),
                  min(3),
                  max(12)
                )
              )
            ),
            userTask(
              ident("userTaskB"),
              form(
                formKey("MyFormKey")
              )
            ),
            scriptTask(
              ident("scriptTask"),
              inlineGroovy(s"println 'hello Scala world'"),
              resultVariable("scriptResult")
            ),
            sequenceFlow(
              ident("flowIsBar"),
              inlineGroovyCond(
                s"""println 'hello'
            $fooVar == 'bar'""")
            )
          )
        )
      )
    println(bpmnModel)
    bpmnModel.toCamunda(path("generatedBpmn.bpmn"))

    

