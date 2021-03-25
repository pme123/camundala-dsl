package camundala.bpmn

import camundala.dsl.DSL
import camundala.model.*
import org.junit.Test
import org.junit.Assert.*

class MergeBpmn2BpmnTest
  extends MergeBpmn2Bpmn :

  val bpmn1 =
    bpmn("myBpmn.bpmn")
      .processes(process("myProcess"))

  val bpmn2 =
    bpmn("myBpmn2.bpmn")
      .processes(process("myProcess2"))

  @Test def mergeTheSameBpmn(): Unit =
    val audit = bpmn1.mergeWith(bpmn1)
    println(s"AUDIT: ${audit.print()}")
    assertEquals("BPMN path match (myBpmn.bpmn).", audit.entries.head.msg)
    assertEquals("Process 'myProcess' exists.", audit.entries.drop(1).head.msg)
    assertEquals(2, audit.entries.size)
    assertEquals(AuditLevel.INFO, audit.maxLevel())

  @Test def mergeDifferentBpmn(): Unit =
    val audit = bpmn1.mergeWith(bpmn2)
    println(s"AUDIT: ${audit.print()}")
    assertEquals("BPMN path has changed: myBpmn.bpmn -> new: myBpmn2.bpmn.", audit.entries.head.msg)
    assertEquals("There is no Process with id 'myProcess' in the new BPMN.", audit.entries.drop(1).head.msg)
    assertEquals("There is no Process with id 'myProcess2' in the existing BPMN.", audit.entries.drop(2).head.msg)
    assertEquals(3, audit.entries.size)
    assertEquals(AuditLevel.WARN, audit.maxLevel())
  

