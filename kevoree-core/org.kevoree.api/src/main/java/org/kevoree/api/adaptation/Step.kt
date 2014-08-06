package org.kevoree.api.adaptation

import java.util.ArrayList

/**
 * Created by duke on 8/6/14.
 */

open class Step {
    var adaptations : MutableList<AdaptationPrimitive> = ArrayList<AdaptationPrimitive>()
    var nextStep : Step? = null
}

class ParallelStep : Step() {

}

class SequentialStep : Step() {

}