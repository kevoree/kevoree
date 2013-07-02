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

trait KevoreeScheduler {

    var step: ParallelStep?
    var currentStep: ParallelStep?
    var adaptationModelFactory: KevoreeAdaptationFactory

    private fun nextStep() {
        if(step == null){
            step = adaptationModelFactory.createParallelStep()
        }
        if(currentStep == null){
            currentStep = step
        }
        if (!step!!.getAdaptations().isEmpty()) {
            step = adaptationModelFactory.createParallelStep()
            currentStep!!.setNextStep(step)
            currentStep = step
        }
    }

    public fun clearSteps() {
        step = null
        currentStep = null
    }

    open fun createNextStep(primitiveType: String, commands: List<AdaptationPrimitive>) {

        if(currentStep == null){
            nextStep()
        }
        if (!commands.isEmpty()) {
            step!!.addAllAdaptations(commands)

            nextStep()
        }
    }


    open fun plan(adaptionModel: AdaptationModel, nodeName: String): AdaptationModel {
        if (!adaptionModel.getAdaptations().isEmpty()) {

            adaptationModelFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()
            val scheduling = SchedulingWithTopologicalOrderAlgo()
            nextStep()
            adaptionModel.setOrderedPrimitiveSet(currentStep)

            //PROCESS STOP
            scheduling.schedule(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StopInstance }, false).forEach {
                p ->
                val commands = ArrayList<AdaptationPrimitive>()
                commands.add(p)
                createNextStep(JavaSePrimitive.StopInstance, commands)
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
            var oldStep = currentStep
            scheduling.schedule(adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StartInstance }, true).forEach {
                p ->
                val commands = ArrayList<AdaptationPrimitive>()
                commands.add(p)
                oldStep = currentStep
                createNextStep(JavaSePrimitive.StartInstance, commands)
            }
            if (step!!.getAdaptations().isEmpty()) {
                oldStep!!.setNextStep(null)
            }
        } else {
            adaptionModel.setOrderedPrimitiveSet(null)
        }
        clearSteps()
        return adaptionModel
    }
}