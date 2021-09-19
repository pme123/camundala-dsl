package camundala
package dev
package test

object PrintBpmnConfigApp extends App with DSL with DslPrinter:

  val adminGroup: BpmnGroup =
    group("admin")
      .name("Administrator")
      .groupType("MyGROUPS")

  private val testUser = user("pme123")
    .name("Muster")
    .firstName("Pascal")
    .email("pascal@muster.ch")
    .group(adminGroup.ref)

  private val testUser2 =
    user("admin")
      .group(adminGroup.ref)

  private val bpmnExample =
    bpmn("myPath")
      .processes(
        process("myIdent")
          .starterGroup(adminGroup.ref)
          .starterUser(testUser.ref)
          .starterUser(testUser2.ref)
        /*  .nodes(
            startEvent("LetsStart")
              .form(formKey("MyForm")),
            serviceTask("ExpressionService")
              .expression("${myVar as String}", "myVar"),
            serviceTask("ExternalTask")
              .externalTask("my-topic"),
            sendTask("DelegateSendTask")
              .delegateExpression("my-delegate"),
            sendTask("DelegateSendTask")
              .javaClass("MyJavaDelegate"),
            userTask("MyUserTask")
              .form(
                textField("textField1").required
                  .minlength(12)
                  .maxlength(33)
                  .prop("width", "12"),
                booleanField("booleanField1")
                  .defaultValue("true"),
                longField("longField1")
                  .max(12)
                  .custom(ident("special1"))
                  .custom(ident("special2"), "hello"),
                enumField("enumField1")
                  .enumValue("de", "Deutsch")
                  .enumValue("fr", "Französisch")
              ),
            businessRuleTask("MyBusinessRule")
              .impl(
                dmnTable("myDmn")
                  .versionTag("v1.0.2")
                  .collectEntries("myResult")
                  .tenantId("myTenant")
              )
          )*/,
        process("process2")
      )

  private val config =
    bpmnsConfig
      .bpmns(bpmnExample, bpmnExample)
      .users(testUser, testUser2)
      .groups(adminGroup)

  bpmnsConfig
    .users(
      user("pme123")
        .name("Muster")
        .firstName("Pascal")
        .email("pascal@muster.ch")
        .group("admin"),
      user("admin")
        .group("admin")
    )
    .groups(
      group("admin")
        .groupType("MyGROUPS")
        .name("Administrator")
    )
    .bpmns(
      bpmn("myPath")
        .processes(
          process("myIdent")
            .starterGroup("admin")
            .starterUser("pme123")
            .starterUser("admin")
           /* .nodes(
              startEvent("LetsStart"),
              serviceTask("ExpressionService"),
              serviceTask("ExternalTask"),
              sendTask("DelegateSendTask"),
              sendTask("DelegateSendTask"),
              userTask("MyUserTask"),
              businessRuleTask("MyBusinessRule")
            )*/,
          process("process2")
           /* .nodes(
            )*/
        ),
      bpmn("myPath")
        .processes(
          process("myIdent")
            .starterGroup("admin")
            .starterUser("pme123")
            .starterUser("admin")
           /* .nodes(
              startEvent("LetsStart"),
              serviceTask("ExpressionService"),
              serviceTask("ExternalTask"),
              sendTask("DelegateSendTask"),
              sendTask("DelegateSendTask"),
              userTask("MyUserTask"),
              businessRuleTask("MyBusinessRule")
            )*/,
          process("process2")
          /*  .nodes(
            )*/
        )
    )

  println(
    DemoProcessRunnerApp.demoConfig
      .print()
      .asString(-1)
  )
