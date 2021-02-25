package camundala.dsl

import camundala.model._

trait processes:
  type ProcessAttributes = CandidateGroups | CandidateUsers | ProcessElements

  def process(ident: Ident,
              attributes: ProcessAttributes*): BpmnProcess =
    BpmnProcess(ident,
      CandidateGroups(attributes.collect { case CandidateGroups(x) => x }.flatten),
      CandidateUsers(attributes.collect { case CandidateUsers(x) => x }.flatten),
      ProcessElements(attributes.collect { case ProcessElements(x) => x }.flatten)
    )

  def starterGroups(groups: GroupRef*) =
    CandidateGroups(groups)

  def starterUsers(users: UserRef*) =
    CandidateUsers(users)

  def elements(processElements: ProcessElement*) =
    ProcessElements(processElements)
