package camundala.examples

import camundala.bpmn.*
import camundala.dsl.DSL

object TwitterProcessRunnerApp extends zio.App with DSL:

  def run(args: List[String]) =
    runnerLogic.exitCode

  private lazy val runnerLogic =
    BpmnRunner(
      RunnerConfig(
        path("bpmns/twitter-cawemo.bpmn"),
        twitterProcess.twitterBpmn,
        path("camunda-demo/src/main/resources/twitter-process.bpmn")
      )
    ).run()

object twitterProcess extends DSL :
  val admin = group("admin")
    .name("Administrator")
    .groupType("system")
  val adminUser = user("admin")
    .name("Administrator")
    .firstName("-")
    .email("myEmail@email.ch")
    .group(admin.ref)

  val isBarVar = "isBar"
  lazy val twitterBpmn =
  bpmn("bpmns/with-ids/twitter-cawemo.bpmn")
    .processes(
        process("TwitterDemoProcess")
          .nodes(
            startEvent("TweetWritten"),
            userTask("ReviewTweet"),
            serviceTask("SendRejectionNotification"),
            serviceTask("PublishOnTwitter"),
            exclusiveGateway("Approved"),
            exclusiveGateway("Join"),
            endEvent("TweetHandled")
          )
          .flows(
            sequenceFlow("SequenceFlow_4_SendRejectionNotification-Join"),
            sequenceFlow("No_Approved-SendRejectionNotification"),
            sequenceFlow("Yes_Approved-PublishOnTwitter"),
            sequenceFlow("SequenceFlow_5_Join-TweetHandled"),
            sequenceFlow("SequenceFlow_3_PublishOnTwitter-Join"),
            sequenceFlow("SequenceFlow_9_TweetWritten-ReviewTweet"),
            sequenceFlow("SequenceFlow_2_ReviewTweet-Approved")
          )
    )
