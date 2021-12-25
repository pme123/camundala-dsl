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
  case class ResultList(resultList: ManyOutResult*)

  private lazy val singleEntryDMN = dmn(
    decisionDefinitionKey = "singleEntry",
    hitPolicy = HitPolicy.UNIQUE,
    in = Input("A"),
    out = SingleEntry(1)
  )

  private lazy val singleResultDMN = dmn(
    decisionDefinitionKey = "singleResult",
    hitPolicy = HitPolicy.UNIQUE,
    in = Input("A"),
    out = SingleResult(ManyOutResult(1, "🤩"))
  )

  private lazy val collectEntriesDMN = dmn(
    decisionDefinitionKey = "collectEntries",
    hitPolicy = HitPolicy.COLLECT,
    in = Input("A"),
    out = CollectEntries(1, 2)
  )

  private lazy val resultListDMN = dmn(
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
