package org.camunda.bpm.spring.boot.example.twitter

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service


@Service(delecateExpr.tweetAdapter)
class TweetContentOfflineDelegate extends JavaDelegate :

  @throws[Exception]
  override def execute(execution: DelegateExecution): Unit =
    val content = execution.getVariable("content").asInstanceOf[String]
    System.out.println("\n\n\n######\n\n\n")
    System.out.println("NOW WE WOULD TWEET: '" + content + "'")
    System.out.println("\n\n\n######\n\n\n")

