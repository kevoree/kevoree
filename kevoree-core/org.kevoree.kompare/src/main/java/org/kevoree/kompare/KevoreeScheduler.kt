package org.kevoree.kompare

import org.kevoreeadaptation.AdaptationModel
import org.kevoree.kompare.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoreeadaptation.AdaptationPrimitive
import java.util.ArrayList

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/09/11
 * Time: 17:54
 */

trait KevoreeScheduler : StepBuilder {

    open fun plan(adaptionModel: AdaptationModel, nodeName: String): AdaptationModel {
        if (!adaptionModel.adaptations.isEmpty()) {

            adaptationModelFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()
            val scheduling = SchedulingWithTopologicalOrderAlgo()

            nextStep()
            adaptionModel.orderedPrimitiveSet = currentSteps
            //STOP INSTANCEs
            var stepToInsert = scheduling.schedule(adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.StopInstance }, false)
            if (stepToInsert != null && !stepToInsert!!.adaptations.isEmpty()) {
                insertStep(stepToInsert!!)
            }

            // REMOVE BINDINGS
            createNextStep(JavaSePrimitive.RemoveBinding, adaptionModel.adaptations.filter { adapt -> (adapt.primitiveType!!.name == JavaSePrimitive.RemoveBinding || adapt.primitiveType!!.name == JavaSePrimitive.RemoveFragmentBinding) })

            // REMOVE INSTANCEs
            createNextStep(JavaSePrimitive.RemoveInstance, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.RemoveInstance })

            // REMOVE TYPEs
            createNextStep(JavaSePrimitive.RemoveType, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.RemoveType })

            // REMOVE DEPLOYUNITs
            createNextStep(JavaSePrimitive.RemoveDeployUnit, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.RemoveDeployUnit })

            // ADD THIRD PARTIES
            createNextStep(JavaSePrimitive.AddThirdParty, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.AddThirdParty })

            // UPDATE DEPLOYUNITs
            createNextStep(JavaSePrimitive.UpdateDeployUnit, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.UpdateDeployUnit })

            // ADD DEPLOYUNITs
            createNextStep(JavaSePrimitive.AddDeployUnit, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.AddDeployUnit })

            // ADD TYPEs
            createNextStep(JavaSePrimitive.AddType, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.AddType })

            // ADD INSTANCEs
            //            createNextStep(JavaSePrimitive.AddInstance, adaptionModel.adaptations.filter{ adapt -> adapt.primitiveType!!.name == JavaSePrimitive.AddInstance })
            adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.AddInstance }.forEach {
                addInstance ->
                val list = ArrayList<AdaptationPrimitive>()
                list.add(addInstance)
                createNextStep(JavaSePrimitive.AddInstance, list)
            }

            // ADD BINDINGs
            createNextStep(JavaSePrimitive.AddBinding, adaptionModel.adaptations.filter { adapt -> (adapt.primitiveType!!.name == JavaSePrimitive.AddBinding || adapt.primitiveType!!.name == JavaSePrimitive.AddFragmentBinding) })

            // UPDATE DICTIONARYs
            createNextStep(JavaSePrimitive.UpdateDictionaryInstance, adaptionModel.adaptations.filter { adapt -> adapt.primitiveType!!.name == JavaSePrimitive.UpdateDictionaryInstance })

            // START INSTANCEs
            stepToInsert = scheduling.schedule(adaptionModel.adaptations.filter {
                adapt ->
                adapt.primitiveType!!.name == JavaSePrimitive.StartInstance
            }, true)
            if (stepToInsert != null && !stepToInsert!!.adaptations.isEmpty()) {
                insertStep(stepToInsert!!)
            }
        } else {
            adaptionModel.orderedPrimitiveSet = null
        }
        clearSteps()
        return adaptionModel
    }
}