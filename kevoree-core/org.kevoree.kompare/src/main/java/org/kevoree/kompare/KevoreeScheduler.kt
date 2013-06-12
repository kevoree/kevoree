package org.kevoree.kompare

import org.kevoreeadaptation.AdaptationModel
import org.kevoree.kompare.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoreeadaptation.ParallelStep
import org.kevoreeadaptation.KevoreeAdaptationFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/09/11
 * Time: 17:54
 */

trait KevoreeScheduler {

    var step : ParallelStep?
    var currentStep : ParallelStep?
    var adaptationModelFactory : KevoreeAdaptationFactory

    private fun nextStep(){
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

    public fun clearSteps(){
        step = null
        currentStep = null
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
                step!!.addAdaptations(p)
                step = adaptationModelFactory.createParallelStep()
                currentStep!!.setNextStep(step)
                currentStep = step
            }
            // REMOVE BINDINGS
            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt ->
                (adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveBinding ||
                adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveFragmentBinding)
            })
            if (!step!!.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep!!.setNextStep(step)
                currentStep = step
            }

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveInstance })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveType })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveDeployUnit })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddThirdParty })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.UpdateDeployUnit })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddDeployUnit })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddType })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddInstance })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt ->
                (adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddBinding ||
                adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddFragmentBinding)
            })

            nextStep()

            step!!.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.UpdateDictionaryInstance })

            nextStep()

            var oldStep = currentStep
            //PROCESS START
            scheduling.schedule(adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StartInstance }, true).forEach {
                p ->
                step!!.addAdaptations(p)
                step = adaptationModelFactory.createParallelStep()
                currentStep!!.setNextStep(step)
                oldStep = currentStep
                currentStep = step
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