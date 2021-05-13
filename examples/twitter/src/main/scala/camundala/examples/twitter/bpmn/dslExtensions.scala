package camundala.examples.twitter.bpmn

import camundala.model.{HasProperties, HasForm}
import camundala.dsl.DSL.*

private val probabilityKey = "probability"
private val kpiRatioKey = "KPI-Ratio"

extension [T](hasProperties: HasProperties[T])

    def probability(number: Int): T =
        hasProperties.prop(probabilityKey, s"$number")

    def kpiRatio(label: String): T =    
        hasProperties.prop(kpiRatioKey, label)

extension [T](hasForm: HasForm[T])

    def createTweetForm: T =
        hasForm.staticForm("forms/createTweet.html")

    def reviewTweetForm: T =    
        hasForm.staticForm("forms/reviewTweet.html")