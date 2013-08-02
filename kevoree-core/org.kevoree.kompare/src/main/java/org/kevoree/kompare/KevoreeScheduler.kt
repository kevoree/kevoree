package org.kevoree.kompare

import org.kevoreeadaptation.AdaptationModel
import org.kevoree.kompare.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoreeadaptation.ParallelStep
import org.kevoreeadaptation.KevoreeAdaptationFactory
import org.kevoreeadaptation.AdaptationPrimitive
import java.util.ArrayList
import org.kevoree.log.Log

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/09/11
 * Time: 17:54
 */

trait KevoreeScheduler : StepBuilder {

    open fun plan(adaptionModel: AdaptationModel, nodeName: String): AdaptationModel {
        if (!adaptionModel.getAdaptations().isEmpty()) {

            adaptationModelFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()
            val scheduling = SchedulingWithTopologicalOrderAlgo()

            nextStep()
            adaptionModel.setOrderedPrimitiveSet(currentSteps)
            //STOP INSTANCEs
            var stepToInsert = scheduling.schedule(adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StopInstance }, false)
            if (stepToInsert != null && !stepToInsert!!.getAdaptations().isEmpty()) {
                insertStep(stepToInsert!!)
            }

            // REMOVE BINDINGS
            createNextStep(JavaSePrimitive.RemoveBinding, adaptionModel.getAdaptations().filter{ adapt -> (adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveBinding || adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveFragmentBinding) })

            // REMOVE INSTANCEs
            createNextStep(JavaSePrimitive.RemoveInstance, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveInstance })

            // REMOVE TYPEs
            createNextStep(JavaSePrimitive.RemoveType, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveType })

            // REMOVE DEPLOYUNITs
            createNextStep(JavaSePrimitive.RemoveDeployUnit, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveDeployUnit })

            // ADD THIRD PARTIES
            createNextStep(JavaSePrimitive.AddThirdParty, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddThirdParty })

            // UPDATE DEPLOYUNITs
            createNextStep(JavaSePrimitive.UpdateDeployUnit, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.UpdateDeployUnit })

            // ADD DEPLOYUNITs
            createNextStep(JavaSePrimitive.AddDeployUnit, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddDeployUnit })

            // ADD TYPEs
            createNextStep(JavaSePrimitive.AddType, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddType })

            // ADD INSTANCEs
            createNextStep(JavaSePrimitive.AddInstance, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddInstance })

            // ADD BINDINGs
            createNextStep(JavaSePrimitive.AddBinding, adaptionModel.getAdaptations().filter{ adapt -> (adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddBinding || adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddFragmentBinding) })

            // UPDATE DICTIONARYs
            createNextStep(JavaSePrimitive.UpdateDictionaryInstance, adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.UpdateDictionaryInstance })

            // START INSTANCEs
            stepToInsert = scheduling.schedule(adaptionModel.getAdaptations().filter{
                adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StartInstance }, true)
            if (stepToInsert != null && !stepToInsert!!.getAdaptations().isEmpty()) {
                insertStep(stepToInsert!!)
            }
        } else {
            adaptionModel.setOrderedPrimitiveSet(null)
        }
        clearSteps()
        return adaptionModel
    }
}