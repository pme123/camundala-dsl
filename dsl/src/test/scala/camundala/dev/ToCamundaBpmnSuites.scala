package camundala
package dev
package test

object ToCamundaBpmnSuites
  extends DefaultRunnableSpec
    with DSL
    with ToCamundaBpmn :

  val fooVar: ProcessVarString = ProcessVarString("fooVar")

  val sequenceFlowIsNotBar = sequenceFlow("flowIsNotBar")

  val bpmnModel: Bpmn =
    bpmn("process")
      .processes(
        process("testDslProcess")
          .starterUser("Darth.Vader")
          .starterGroup("admin")
          .nodes(
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
                  .required,
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
              ),
            userTask("userTaskB")
              .form(
                formKey("MyFormKey")
              ),
            scriptTask("scriptTask")
              .inlineGroovy(s"println 'hello Scala world'")
              .resultVariable("scriptResult"),
            exclusiveGateway("gatewayFork")
              .defaultFlow(sequenceFlowIsNotBar.ref)
          )
          .flows(
            sequenceFlow("flowIsBar")
              .inlineGroovy(
                s"""println 'hello'
            $fooVar == 'bar'"""),
            sequenceFlowIsNotBar

          )
      )

  def spec = suite("ToCamundaBpmnSuites")(
    testM("merge DSL Bpmn with BPMN XML") {
      assertM(bpmnModel.toCamunda(path("./dsl/src/test/cawemo"), path("./dsl/src/test/cawemo/output")))(isUnit)
    }
  )



