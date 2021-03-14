import camundala.dsl.DSL
import camundala.dsl.DSL._
import camundala.bpmn._

object Main extends App with DSL :


  val adminGroup = group(ident("admin"), name("Administrator"), groupType("MyGROUP"))

  private val testUser = user(
    username("pme123"),
    name("Muster"),
    firstName("Pascal"),
    email("pascal@muster.ch"),
    groupRefs(
      adminGroup.ref
    )
  )
  private val bpmnExample =
    bpmn("myPath")
      .processes(
        process("myIdent")
          .starterGroups(
            adminGroup.ref
          )
          .starterUsers(
            testUser.ref
          )
          .elements(
            startEvent("LetsStart")
              .form(formKey("MyForm"))
            ,
            serviceTask("ExpressionService")
            .expression("${myVar as String}", "myVar")
          ,
            serviceTask("ExternalTask")
            .externalTask("my-topic")
            ,
            sendTask("DelegateSendTask")
            .delegateExpression("my-delegate")
            ,
            sendTask("DelegateSendTask")
              .javaClass("MyJavaDelegate")
            ,
            userTask("MyUserTask")
              .form(
                textField("textField1")
                  .required
                  .minlength(12)
                  .maxlength(33)
                  .prop("width", "12")
                ,
                booleanField("booleanField1")
                  .defaultValue("true")
                ,
                longField("longField1")
                  .max(12)
                  .custom(ident("special1"))
                  .custom(ident("special2"), "hello")
                  ,
                enumField("enumField1")
                  .enumValue("de", "Deutsch")
                  .enumValue("fr", "Französisch")
                )
              ,
            businessRuleTask("MyBusinessRule")
              .impl(
                dmn("myDmn")
                .versionTag("v1.0.2")
                .collectEntries("myResult")
                .tenantId("myTenant")
              )
          ),
        process("process2")
      )

  private val config =
    bpmnsConfig
      .bpmns(bpmnExample)
      .users(testUser)
      .groups(adminGroup)

  println(
    config.stringify()
  )

