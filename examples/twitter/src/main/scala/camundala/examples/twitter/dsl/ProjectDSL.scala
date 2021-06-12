package camundala
package examples.twitter
package dsl

import model.{HasProperties, HasMaybeForm}
import model.{HasTaskImplementation, ServiceTask}
import model.TaskImplementation.DelegateExpression

final val emailAdapter = "emailAdapter"
final val tweetAdapter = "tweetAdapter"

trait ProjectDSL extends DSL:

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

    extension[T](hasTaskImpl: HasTaskImplementation[T])
        def emailDelegate =
            hasTaskImpl.taskImplementation(
            DelegateExpression(emailAdapter)
            )

        def tweetDelegate =
            hasTaskImpl.taskImplementation(
            DelegateExpression(tweetAdapter)
            )