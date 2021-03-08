import camundala.dsl.DSL
import camundala.dsl.DSL._
import camundala.model._

object Main extends App with DSL :


  val adminGroup = group(ident("admin"), name("Administrator"), groupType("MyGROUP"))

  private val testUser: BpmnUser = user(
    username("pme123"),
    name("Muster"),
    firstName("Pascal"),
    email("pascal@muster.ch"),
    groupRefs(
      adminGroup.ref
    )
  )
  private val bpmnExample =
    bpmn(
      path("myPath"),
      process(
        ident("myIdent"),
        starterGroups(
          adminGroup.ref
        ),
        starterUsers(
          testUser.ref
        ),
        elements(
          startEvent(
            ident("LetsStart"),
            form(formKey("MyForm"))
          ),
          serviceTask(
            ident("ExpressionService"),
            expression("${myVar as String}", "myVar")
          ),
          serviceTask(
            ident("ExternalTask"),
            externalTask("my-topic")
          ),
          sendTask(
            ident("DelegateSendTask"),
            delegateExpression("my-delegate")
          ),
          sendTask(
            ident("DelegateSendTask"),
            javaClass("MyJavaDelegate")
          ),
          userTask(
            ident("MyUserTask"),
            form(
              textField(
                ident("textField1"),
                required,
                minlength(12),
                maxlength(33),
                prop(
                  ident("width"), "12")
              ),
              booleanField(
                ident("booleanField1"),
                defaultValue("true")
              ),
              longField(ident("longField1"),
                max(12),
                custom(ident("special1")),
                custom(ident("special2"), "hello")),
              enumField(ident("enumField1"),
                enumValue(ident("de"), name("Deutsch")),
                enumValue(ident("fr"), name("Französisch"))
              )
            )),
          businessRuleTask(
            ident("MyBusinessRule"),
            dmn(decisionRef("myDmn"),
              versionTag("v1.0.2"),
              resultVariable(name("myResult"), collectEntries),
              tenantId("myTenant"))
          )
        )),
      process(ident("process2"))
    )

  println(
    bpmnsConfig(
      users(testUser),
      groups(adminGroup),
      bpmns(bpmnExample)
    ).stringify()
  )

