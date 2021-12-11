package camundala
package utest

import os.{Path, pwd}

trait TestDsl:

  def testConfig: TestConfig =
    TestConfig()

  extension (testConfig: TestConfig)

    def deployments(deployments: Path*): TestConfig =
      testConfig.copy(deploymentResources = deployments.toSet)
    def registries(sRegistries: ServiceRegistry*): TestConfig =
      testConfig.copy(serviceRegistries = sRegistries.toSet)

  end extension

  def serviceRegistry(key: String, value: Any) = ServiceRegistry(key, value)
  val baseResource = pwd / "src" / "main" / "resources"
  def formResource = baseResource / "static"
  
  