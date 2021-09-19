package camundala
package test

import camundala.model.{InOutObject, ServiceTask}

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

  extension (userTask: UserTask)
    def step(data: InOutObject) =
      UserTaskStep(userTask, data)
  end extension

  extension (serviceTask: ServiceTask)
    def step(data: Option[InOutObject] = None) =
      ServiceTaskStep(serviceTask, data)
  end extension

  extension (startEvent: StartEvent)
    def start(data: InOutObject) =
      StartProcessStep(startEvent, data)
  end extension

  extension (endEvent: EndEvent)
    def finish(data: InOutObject*) =
      EndStep(endEvent, data)
  end extension

end TestDSL

object TestDSL extends TestDSL
