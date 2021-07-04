package camundala
package examples.invoice
package services

import org.camunda.bpm.engine.variable.value.FileValue
import java.util.logging.Logger

/**
 * <p>This is an empty service implementation illustrating how to use a plain
 * Java Class as a BPMN 2.0 Service Task delegate.</p>
 */
@Service(dsl.archiveAdapter)
class ArchiveInvoiceService extends JavaDelegate :
  final private val LOGGER = Logger.getLogger(classOf[ArchiveInvoiceService].getName)

  @throws[Exception]
  override def execute(execution: DelegateExecution): Unit =
    val shouldFail = execution.getVariable("shouldFail")
    val invoiceDocumentVar: FileValue = execution.getVariableTyped("invoiceDocument")
    if (shouldFail != null && shouldFail.asInstanceOf[Boolean]) throw new ProcessEngineException("Could not archive invoice...")
    else LOGGER.info("\n\n  ... Now archiving invoice " + execution.getVariable("invoiceNumber") + ", filename: " + invoiceDocumentVar.getFilename + " \n\n")
