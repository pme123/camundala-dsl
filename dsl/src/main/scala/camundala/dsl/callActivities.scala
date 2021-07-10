package camundala
package dsl

import camundala.model.{CallActivity, VariableAssignment}

trait callActivities :

  def callActivity(ident: String) =
    CallActivity(ident)

  extension (callActivity: CallActivity)
    def binding(refBinding: RefBinding): CallActivity =
      callActivity.copy(binding = refBinding)

    def latest: CallActivity =
      binding(RefBinding.Latest)

    def deployment: CallActivity =
      binding(RefBinding.Deployment)

    def version(v: String): CallActivity = binding(RefBinding.Version(v))

    def versionTag(tag: String): CallActivity =
      binding(RefBinding.VersionTag(tag))

    def businessKey(expr: String): CallActivity =
      callActivity.copy(businessKey =
        Some(VariableAssignment.Expression(expr))
      )

    def processBusinessKey: CallActivity =
      businessKey("#{execution.processBusinessKey}")

    def calledElement(elem: String) =
      callActivity.copy(calledElement =
        CalledElement(elem)
      )

    def tenantId(id: String): CallActivity =
      callActivity.copy(tenantId = Some(TenantId(id)))
