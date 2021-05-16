package camundala.examples.twitter.services

import camundala.examples.twitter.dsl.delecateExpr
import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}
import org.springframework.stereotype.Service

@Service(delecateExpr.emailAdapter)
class RejectionNotificationDelegate extends JavaDelegate :

  @throws[Exception]
  override def execute(execution: DelegateExecution): Unit =
    val content = execution.getVariable("content").asInstanceOf[String]
    val comments = execution.getVariable("comments").asInstanceOf[String]
    println("Hi!\n\n" + "Unfortunately your tweet has been rejected.\n\n" + "Original content: " + content + "\n\n" + "Comment: " + comments + "\n\n" + "Sorry, please try with better content the next time :-)")