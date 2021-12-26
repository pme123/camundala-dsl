package camundala
package camunda

import bpmn.*
import os.Path

case class RunnerConfig(
                         projectName: String,
                         cawemoFolder: Path,
                         withIdFolder: Path,
                         generatedFolder: Path,
                         bpmnsConfig: BpmnsConfig
                       )

case class BpmnsConfig(bpmns: Bpmns = Bpmns.none,
                       dmns: Dmns = Dmns.none,
                       //    groups: BpmnGroups,
                       //    users: BpmnUsers = BpmnUsers.none
                      )

object BpmnsConfig:
  def none = BpmnsConfig(
    Bpmns.none,
    Dmns.none,
    //  BpmnGroups.none,
    //  BpmnUsers.none
  )