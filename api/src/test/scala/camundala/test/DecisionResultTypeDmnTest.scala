package camundala
package test

import bpmn.*
import org.junit.Test
import test.*
import os.{Path, ResourcePath}
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

class DecisionResultTypeDmnTest extends DmnTestRunner, PureDsl:

  val dmnPath: ResourcePath = baseResource / "DecisionResultTypes.dmn"

  case class Input(letter: String)
  // Single Output Parameter
  case class SingleEntry(index: Int)
  case class CollectEntries(indexes: Int*)
  // Many Output Parameter
  case class ManyOutResult(index: Int, emoji: String)
  case class SingleResult(result: ManyOutResult)
  case class ResultList(results: ManyOutResult*)

  private lazy val singleEntryDMN = singleEntry(
    decisionDefinitionKey = "singleEntry",
    hitPolicy = HitPolicy.UNIQUE,
    in = Input("A"),
    out = SingleEntry(1)
  )

  private lazy val singleResultDMN = singleResult(
    decisionDefinitionKey = "singleResult",
    hitPolicy = HitPolicy.UNIQUE,
    in = Input("A"),
    out = SingleResult(ManyOutResult(1, "🤩"))
  )

  private lazy val collectEntriesDMN = collectEntries(
    decisionDefinitionKey = "collectEntries",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = CollectEntries(1, 2)
  )

  private lazy val resultListDMN = resultList(
    decisionDefinitionKey = "resultList",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = ResultList(ManyOutResult(1, "🤩"), ManyOutResult(2, "😂"))
  )

  @Test
  def testSingleEntry(): Unit =
    test(singleEntryDMN)

  @Test
  def testSingleResult(): Unit =
    test(singleResultDMN)

  @Test
  def testCollectEntries(): Unit =
    test(collectEntriesDMN)

  @Test
  def testResultList(): Unit =
    test(resultListDMN  )

  @Test(expected = classOf[IllegalArgumentException])
  def testSingleEntryBadHitpolicy(): Unit =
    test(singleEntryDMNBadHitpolicy)

  @Test(expected = classOf[IllegalArgumentException])
  def testSingleEntryBadOutput(): Unit =
    test(singleEntryDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testSingleResultBadHitpolicy(): Unit =
    test(singleResultDMNBadHitpolicy)

  @Test(expected = classOf[IllegalArgumentException])
  def testSingleResultBadOutput(): Unit =
    test(singleResultDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testCollectEntriesBadHitpolicy(): Unit =
    test(collectEntriesDMNBadHitpolicy)

  @Test(expected = classOf[IllegalArgumentException])
  def testCollectEntriesBadOutput(): Unit =
    test(collectEntriesDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testCollectEntriesEmptySeq(): Unit =
    test(collectEntriesDMNEmptySeq)

  @Test(expected = classOf[IllegalArgumentException])
  def testResultListBadHitpolicy(): Unit =
    test(resultListDMNBadHitpolicy)

  @Test(expected = classOf[IllegalArgumentException])
  def testResultListBadOutput(): Unit =
    test(resultListDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testResultListEmptySeq(): Unit =
    test(resultListDMNEmptySeq)

  private def singleEntryDMNBadHitpolicy = singleEntry(
    decisionDefinitionKey = "singleEntry",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = SingleEntry(1)
  )

  private def singleEntryDMNBadOutput = singleEntry(
    decisionDefinitionKey = "singleEntry",
    hitPolicy = HitPolicy.UNIQUE,
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )

  private def singleResultDMNBadHitpolicy = singleResult(
    decisionDefinitionKey = "singleResult",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = SingleResult(ManyOutResult(1, "🤩"))
  )

  private def singleResultDMNBadOutput = singleResult(
    decisionDefinitionKey = "singleResult",
    hitPolicy = HitPolicy.UNIQUE,
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )

  private def collectEntriesDMNBadHitpolicy = collectEntries(
    decisionDefinitionKey = "collectEntries",
    hitPolicy = HitPolicy.FIRST,
    in = Input("A"),
    out = CollectEntries(1)
  )

  private def collectEntriesDMNBadOutput = collectEntries(
    decisionDefinitionKey = "collectEntries",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )
  private def collectEntriesDMNEmptySeq = collectEntries(
    decisionDefinitionKey = "collectEntries",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = CollectEntries()
  )

  private def resultListDMNBadHitpolicy = resultList(
    decisionDefinitionKey = "resultList",
    hitPolicy = HitPolicy.COLLECT_SUM,
    in = Input("A"),
    out = ResultList(ManyOutResult(1, "🤩"), ManyOutResult(2, "😂"))
  )

  private def resultListDMNBadOutput = resultList(
    decisionDefinitionKey = "resultList",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )
  private def resultListDMNEmptySeq = resultList(
    decisionDefinitionKey = "resultList",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = ResultList()
  )