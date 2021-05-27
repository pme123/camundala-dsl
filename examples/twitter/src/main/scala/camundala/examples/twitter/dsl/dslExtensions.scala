package camundala.examples.twitter.bpmn

import camundala.model.{HasProperties, HasMaybeForm}
import camundala.dsl.DSL.*

private val probabilityKey = "probability"
private val kpiRatioKey = "KPI-Ratio"

extension [T](hasProperties: HasProperties[T])

    def probability(number: Int): T =
        hasProperties.prop(probabilityKey, s"$number")

    def kpiRatio(label: String): T =    
        hasProperties.prop(kpiRatioKey, label)

final val createTweetFormPath = "forms/createTweet.html"        
final val reviewTweetFormPath = "forms/reviewTweet.html"        
extension [T](hasForm: HasMaybeForm[T])

    def createTweetForm: T =
        hasForm.staticForm(createTweetFormPath)

    def reviewTweetForm: T =    
        hasForm.staticForm(reviewTweetFormPath)
