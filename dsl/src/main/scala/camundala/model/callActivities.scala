package camundala
package model

opaque type CalledElement = String

object CalledElement:
  def apply(calledElement: String): CalledElement = calledElement

case class CallActivity(
    calledElement: CalledElement,
    binding: RefBinding = RefBinding.Latest,
    businessKey: Option[VariableAssignment.Expression] = None,
    tenantId: Option[TenantId] = None,
    activity: Activity,
    inVariables: Seq[InOutVariable] = Seq.empty,
    outVariables: Seq[InOutVariable] = Seq.empty
) extends HasActivity[CallActivity],
      HasInVariables[CallActivity],
      HasOutVariables[CallActivity]:

  def elemKey: ElemKey = ElemKey.callActivities

  def withActivity(activity: camundala.model.Activity): CallActivity =
    copy(activity = activity)
  def withIns(inVariables: Seq[InOutVariable]): CallActivity =
    copy(inVariables = inVariables)
  def withOuts(outVariables: Seq[InOutVariable]): CallActivity =
    copy(outVariables = outVariables)

object CallActivity:

  def apply(ident: String): CallActivity =
    CallActivity(CalledElement(ident), activity = Activity(ident))
