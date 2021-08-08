package camundala
package test

trait TestDSL:

  def tester(process: BpmnProcess)(testConfig: TestConfig): BpmnProcessTester =
    BpmnProcessTester(process, testConfig)

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

  def testCase(name: String)(steps: BpmnTestStep*) =
    BpmnTestCase(name, steps)

  extension(userTask: UserTask)
    def step(data: TestData) =
      UserTaskStep(userTask, data)
  end extension

  extension(startEvent: StartEvent)
    def step(data: TestData) =
      StartProcessStep(startEvent, data)
  end extension

end TestDSL

object TestDSL extends TestDSL
