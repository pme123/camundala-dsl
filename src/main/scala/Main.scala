import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnParser

object Main:

  def main(args: Array[String]): Unit = 
    println("Hello world!")
    println(msg)

  val msg: String = 
    "I was compiled by dotty :)"
