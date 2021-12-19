package camundala
package utest

import domain.*
import bpmn.*
import camundala.bpmn.{Activity, DecisionDmn, Process}
import org.camunda.bpm.dmn.engine.{DmnDecision, DmnDecisionResult, DmnEngine}
import org.camunda.bpm.dmn.engine.test.DmnEngineRule
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{
  assertThat,
  repositoryService,
  runtimeService,
  task
}
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Assert.assertEquals
import org.junit.{Before, Rule}
import org.mockito.MockitoAnnotations
import os.Path
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.model.dmn.DmnModelInstance

import scala.collection.immutable
import scala.jdk.CollectionConverters.*

trait DmnTestRunner extends TestDsl:

  def dmnPath: Path

  @Rule
  lazy val dmnEngineRule = new DmnEngineRule()

  lazy val dmnEngine: DmnEngine = dmnEngineRule.getDmnEngine

  lazy val dmnInputStream =
    new java.io.ByteArrayInputStream(os.read.bytes(dmnPath))

  def test[
      In <: Product,
      Out <: Product
  ](decisionDmn: DecisionDmn[In, Out]): Unit =
    val variables: VariableMap = Variables.createVariables
    for (k, v) <- decisionDmn.inOutDescr.in.asDmnVars()
    yield variables.putValue(k, v)

    val cDecision: DmnDecision =
      dmnEngine.parseDecision(decisionDmn.decisionDefinitionKey, dmnInputStream)
    val dependentDecisions = cDecision.getRequiredDecisions.asScala
    variables.putAll(
      dependentDecisions
        .flatMap(d => {
          val r = dmnEngine.evaluateDecision(d, variables)
          r.getFirstResult.getEntryMap.asScala
        })
        .toMap
        .asJava
    )
    val result = dmnEngine.evaluateDecisionTable(cDecision, variables)

    val resultList = result.getResultList.asScala
    decisionDmn.decisionResultType match
      case DecisionResultType.singleEntry => // SingleEntry
        println(s"singleEntry: ${result.getSingleEntry}")
      case DecisionResultType.singleResult =>
        println(s"singleResult: ${result.getSingleResult.getEntryMap}")
      case DecisionResultType.collectEntries =>
        assert(
          decisionDmn.out.isInstanceOf[ManyInOut[?]],
          "For DecisionResultType.collectEntries you need to have ManyInOut[?] object."
        )
        val expResults: Seq[Map[String, Any]] = decisionDmn.out match
          case manyInOut: ManyInOut[?] =>
            manyInOut.toSeq.map(_.asDmnVars())
          case m =>
            Seq(m.asDmnVars())

        assert(expResults.size == resultList.size)
        for i <- expResults.indices
        yield
          val rMap = resultList(i)
          expResults(i).foreach { case (expKey, expValue) =>
            println(s"assert $expKey: ${rMap.get(expKey)} == $expValue")
            assert(rMap.get(expKey) == expValue)
          }

      case DecisionResultType.resultList =>
        println(s"resultList: $resultList")
