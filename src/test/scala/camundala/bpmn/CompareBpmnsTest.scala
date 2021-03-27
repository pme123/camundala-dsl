package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.junit.Test
import org.junit.Assert.*

class CompareBpmnsTest
  extends CompareBpmns :

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

  @Test def mergeTheSameBpmn(): Unit =
    val audit = bpmn1.mergeWith(bpmn1)
    audit.print()
    assertEquals("BPMN path match (myBpmn.bpmn).", audit.entries.head.msg)
    assertEquals("Process 'myProcess' exists.", audit.entries.drop(1).head.msg)
    assertEquals("'LetsStart' exists.", audit.entries.drop(2).head.msg)
    assertEquals("'serviceTaskA' exists.", audit.entries.drop(3).head.msg)
    assertEquals(7, audit.entries.size)
    assertEquals(AuditLevel.INFO, audit.maxLevel())

  @Test def mergeDifferentBpmn(): Unit =
    val audit = bpmn1.mergeWith(bpmn2)
    audit.print()
    assertEquals("BPMN path has changed: myBpmn.bpmn -> new: myBpmn2.bpmn.", audit.entries.head.msg)
    assertEquals("There is no Process with id 'myProcess' in the new BPMN.", audit.entries.drop(1).head.msg)
    assertEquals("There is no Process with id 'myProcess2' in the existing BPMN.", audit.entries.drop(2).head.msg)
    assertEquals(3, audit.entries.size)
    assertEquals(AuditLevel.WARN, audit.maxLevel())

  @Test def mergeDifferentElements(): Unit =
    val audit = bpmn1.mergeWith(bpmn3)
    audit.print()
    assertEquals("BPMN path match (myBpmn.bpmn).", audit.entries.head.msg)
    assertEquals("Process 'myProcess' exists.", audit.entries.drop(1).head.msg)
    assertEquals("There is no 'serviceTaskA' in the new BPMN.", audit.entries.drop(3).head.msg)
    assertEquals("There is no 'serviceTaskB' in the existing BPMN.", audit.entries.drop(7).head.msg)
    assertEquals(8, audit.entries.size)
    assertEquals(AuditLevel.WARN, audit.maxLevel())
