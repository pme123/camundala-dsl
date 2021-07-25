package camundala
package test

case class TestConfig(
    deploymentResources: Set[String] = Set.empty,
    serviceRegistries: Set[ServiceRegistry] = Set.empty
)
case class ServiceRegistry(key: String, value: Any)

case class BpmnProcessTester(
    process: BpmnProcess,
    testConfig: TestConfig = TestConfig(),
    cases: Seq[BpmnTestCase] = Nil
)

case class BpmnTestCase(name: String, steps: Seq[BpmnTestStep] = Nil)

case class BpmnTestStep(activity: Activity, in: TestInOut, out: TestInOut)

trait TestInOut extends Product:

  def asVars(): Map[String, Any] =
    productElementNames
      .zip(productIterator)
      .toMap

trait TestDSL:

  def tester(process: BpmnProcess) =
    BpmnProcessTester(process)

  extension (processTester: BpmnProcessTester)
    def config(testConfig: TestConfig): BpmnProcessTester =
      processTester.copy(testConfig = testConfig)
    def cases(testCases: BpmnTestCase*): BpmnProcessTester =
      processTester.copy(cases = cases)

  end extension

  def testConfig =
    TestConfig()

  extension (testConfig: TestConfig)

    def deployments(deployments: String*): TestConfig =
      testConfig.copy(deploymentResources = deployments.toSet)
    def registries(sRegistries: ServiceRegistry*): TestConfig =
      testConfig.copy(serviceRegistries = sRegistries.toSet)

  end extension

  def serviceRegistry(key: String, value: Any) = ServiceRegistry(key, value)
  def formResource(path: String) = s"static/$path"

  def testCase(name: String)(steps: BpmnTestStep*) = BpmnTestCase(name, steps)

  def testStep(activity: Activity, in: TestInOut, out: TestInOut) =
    BpmnTestStep(activity, in, out)

object TestDSL extends TestDSL
