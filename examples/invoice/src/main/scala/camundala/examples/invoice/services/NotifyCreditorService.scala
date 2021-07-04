package camundala
package examples.invoice
package services

import java.util.logging.Logger

@Service(dsl.notifyCreditorAdapter)
class NotifyCreditorService extends JavaDelegate {
  final private val LOGGER = Logger.getLogger(classOf[NotifyCreditorService].getName)

  @throws[Exception]
  def execute(execution: DelegateExecution): Unit = {
    LOGGER.info("\n\n  ... Now notifying creditor " + execution.getVariable("creditor") + "\n\n")
  }
}

