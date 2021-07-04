package camundala.examples.invoice

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.example.invoice.InvoiceProcessApplication
import org.springframework.context.event.EventListener

import java.beans.BeanProperty
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableProcessApplication
class InvoiceServletProcessApplication

object InvoiceServletProcessApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[InvoiceServletProcessApplication], args: _*)

  val invoicePa = new InvoiceProcessApplication()
  /*
  @PostConstruct def deployInvoice(): Unit = {
    println(s"processEngine $processEngine")
    val classLoader = this.getClass.getClassLoader
    if (processEngine.getIdentityService.createUserQuery.list.isEmpty)
      processEngine.getRepositoryService.createDeployment
        .addInputStream(
          "invoice.v1.bpmn",
          classLoader.getResourceAsStream("invoice.v1.bpmn")
        )
        .addInputStream(
          "reviewInvoice.bpmn",
          classLoader.getResourceAsStream("reviewInvoice.bpmn")
        )
        .deploy
  }
   */
  @EventListener
  def onPostDeploy(event: PostDeployEvent): Unit = {
    invoicePa.startFirstProcess(event.getProcessEngine)
  }
