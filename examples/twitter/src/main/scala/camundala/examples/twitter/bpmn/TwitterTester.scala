package camundala
package examples.twitter
package bpmn
import test.*
import ExampleTwitter.bpmns.processes.* 

object TwitterTester extends App, TestDSL :
  val twitterTester = 
    tester(TwitterDemoProcess)
  /*.cases(
  testCase("Happy Path")(
     //testStep("")
   )
    )*/
  println(s"twitterTester: $twitterTester")
  
  case class Input(content: String)
    extends TestInOut