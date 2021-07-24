package camundala
package test

import camundala.model.{Activity, BpmnProcess}

case class BpmnProcessTester(process: BpmnProcess, cases: Seq[BpmnTestCase] = Nil)

case class BpmnTestCase(name: String, steps: Seq[BpmnTestStep] = Nil)

case class BpmnTestStep(activity: Activity, in: TestInOut, out: TestInOut)

trait TestInOut extends Product :

  def asVars(): Map[String, Any] =
    productElementNames
      .zip(productIterator)
      .toMap

trait TestDSL :

  def tester(process: BpmnProcess)(testCases: BpmnTestCase*) = BpmnProcessTester(process, testCases)

  extension(processTester: BpmnProcessTester)
    def cases(testCases: BpmnTestCase*): BpmnProcessTester =
      processTester.copy(cases = cases)

  end extension

  def testCase(name: String)(steps: BpmnTestStep*) = BpmnTestCase(name, steps)

  def testStep(activity: Activity, in: TestInOut, out: TestInOut) =
    BpmnTestStep(activity, in, out)

object TestDSL extends TestDSL

