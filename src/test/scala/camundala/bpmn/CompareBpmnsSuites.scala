package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.junit.Test
import org.junit.Assert.*
import zio.test.*
import zio.test.Assertion.*

object CompareBpmnsSuites
  extends DefaultRunnableSpec
    with CompareBpmns :

  val bpmn1 =
    bpmn("myBpmn.bpmn")
      .processes(
        process("myProcess")
          .elements(
            startEvent("LetsStart"),
            serviceTask("serviceTaskA"),
            userTask("userTaskA"),
            businessRuleTask("Rulez"),
            flow("no_1234_1234")
          )
      )

  val bpmn2 =
    bpmn("myBpmn2.bpmn")
      .processes(process("myProcess2"))

  val bpmn3 =
    bpmn("myBpmn.bpmn")
      .processes(
        process("myProcess")
          .elements(
            startEvent("LetsStart"),
            serviceTask("serviceTaskB"),
            userTask("userTaskA"),
            businessRuleTask("Rulez"),
            flow("no_1234_1234")
          )
      )

  def spec = suite("CompareBpmnsSuites") (
    test("compare the same BPMNs") {
      val audit = bpmn1.compareWith(bpmn1)
      audit.print()
      assert(audit.entries.head.msg)(equalTo("BPMN path match (myBpmn.bpmn).")) &&
        assert(audit.entries.drop(1).head.msg)(equalTo("Process 'myProcess' exists.")) &&
        assert(audit.entries.drop(2).head.msg)(equalTo("'LetsStart' exists.")) &&
        assert(audit.entries.drop(3).head.msg)(equalTo("'serviceTaskA' exists.")) &&
        assert(audit.entries.size)(equalTo(7)) &&
        assert(audit.maxLevel())(equalTo(AuditLevel.INFO))
    },
    test("compare different BPMNs") {
      val audit = bpmn1.compareWith(bpmn2)
      audit.print()
      assert(audit.entries.head.msg)(equalTo("BPMN path has changed: myBpmn.bpmn -> new: myBpmn2.bpmn.")) &&
        assert(audit.entries.drop(1).head.msg)(equalTo("There is no Process with id 'myProcess' in the new BPMN.")) &&
        assert(audit.entries.drop(2).head.msg)(equalTo("There is no Process with id 'myProcess2' in the existing BPMN.")) &&
        assert(audit.entries.size)(equalTo(3)) &&
        assert(audit.maxLevel())(equalTo(AuditLevel.WARN))
    },
    test("compare different BPMN elements"){
      val audit = bpmn1.compareWith(bpmn3)
      audit.print()
      assert(audit.entries.head.msg)(equalTo("BPMN path match (myBpmn.bpmn).")) &&
        assert(audit.entries.drop(1).head.msg)(equalTo("Process 'myProcess' exists.")) &&
        assert(audit.entries.drop(3).head.msg)(equalTo("There is no 'serviceTaskA' in the new BPMN.")) &&
        assert(audit.entries.drop(7).head.msg)(equalTo("There is no 'serviceTaskB' in the existing BPMN.")) &&
        assert(audit.entries.size)(equalTo(8)) &&
        assert(audit.maxLevel())(equalTo(AuditLevel.WARN))
    }
  )

