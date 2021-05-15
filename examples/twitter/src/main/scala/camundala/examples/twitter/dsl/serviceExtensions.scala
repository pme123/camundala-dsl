package camundala.examples.twitter.dsl

import camundala.model.TaskImplementation.DelegateExpression
import camundala.model.{HasTaskImplementation, ServiceTask}
import org.camunda.bpm.engine.delegate.DelegateExecution

object delecateExpr :
  final val emailAdapter = "emailAdapter"
  final val tweetAdapter = "tweetAdapter"

extension [T](hasTaskImpl: HasTaskImplementation[T])
  def emailDelegate =
    hasTaskImpl.taskImplementation(DelegateExpression(delecateExpr.emailAdapter))

  def tweetDelegate =
    hasTaskImpl.taskImplementation(DelegateExpression(delecateExpr.tweetAdapter))
