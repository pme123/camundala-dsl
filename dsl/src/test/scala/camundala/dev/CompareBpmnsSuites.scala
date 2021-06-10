package camundala.dev

import camundala.dsl.DSL
import camundala.model.*
import org.junit.Test
import org.junit.Assert.*
import zio.test.*
import zio.test.Assertion.*

object CompareBpmnsSuites extends DefaultRunnableSpec with CompareBpmns:

  val bpmn1 =
    bpmnsConfig
      .bpmns(
        bpmn("myBpmn.bpmn")
          .processes(
            process("myProcess")
              .nodes(
                startEvent("LetsStart"),
                serviceTask("serviceTaskA"),
                userTask("userTaskA"),
                businessRuleTask("Rulez")
              )
              .flows(
                flow("no_1234_1234")
              )
          )
      )
  val bpmn2 =
    bpmnsConfig
      .bpmns(
        bpmn("myBpmn2.bpmn")
          .processes(process("myProcess2"))
      )

  val bpmn3 =
    bpmnsConfig
      .bpmns(
        bpmn("myBpmn.bpmn")
          .processes(
            process("myProcess")
              .nodes(
                startEvent("LetsStart"),
                serviceTask("serviceTaskB"),
                userTask("userTaskA"),
                businessRuleTask("Rulez")
              )
              .flows(
                flow("no_1234_1234")
              )
          )
      )

  def spec = suite("CompareBpmnsSuites")(
    test("compare the same BPMNs") {
      val audit = bpmn1.compareWith(bpmn1)
      audit.print()
      assert(audit.entries.head.msg)(
        equalTo("BPMN ident match (myBpmn.bpmn).")
      ) &&
      assert(audit.entries.drop(1).head.msg)(
        equalTo("Process 'myProcess' exists.")
      ) &&
      assert(audit.entries.drop(2).head.msg)(equalTo("'LetsStart' exists.")) &&
      assert(audit.entries.drop(3).head.msg)(
        equalTo("'serviceTaskA' exists.")
      ) &&
      assert(audit.entries.size)(equalTo(7)) &&
      assert(audit.maxLevel())(equalTo(AuditLevel.INFO))
    },
    test("compare different BPMNs") {
      val audit = bpmn1.compareWith(bpmn2)
      audit.print()
      assert(audit.entries.head.msg)(
        equalTo("BPMN ident has changed: myBpmn.bpmn -> new Bpmns: myBpmn2.bpmn.")
      ) &&
      assert(audit.entries.size)(equalTo(1)) &&
      assert(audit.maxLevel())(equalTo(AuditLevel.WARN))
    },
    test("compare different BPMN elements") {
      val audit = bpmn1.compareWith(bpmn3)
      audit.print()
      assert(audit.entries.head.msg)(
        equalTo("BPMN ident match (myBpmn.bpmn).")
      ) &&
      assert(audit.entries.drop(1).head.msg)(
        equalTo("Process 'myProcess' exists.")
      ) &&
      assert(audit.entries.drop(3).head.msg)(
        equalTo("There is no 'serviceTaskA' in the new BPMN.")
      ) &&
      assert(audit.entries.drop(6).head.msg)(
        equalTo("There is no 'serviceTaskB' in the existing BPMN.")
      ) &&
      assert(audit.entries.size)(equalTo(8)) &&
      assert(audit.maxLevel())(equalTo(AuditLevel.WARN))
    }
  )
