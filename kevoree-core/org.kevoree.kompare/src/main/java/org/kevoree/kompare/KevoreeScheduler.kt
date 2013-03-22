package org.kevoree.kompare

import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.kompare.scheduling.SchedulingWithTopologicalOrderAlgo

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/09/11
 * Time: 17:54
 */

trait KevoreeScheduler {




    fun plan(adaptionModel: AdaptationModel, nodeName: String): AdaptationModel {
        if (!adaptionModel.getAdaptations().isEmpty()) {

            val adaptationModelFactory = org.kevoreeAdaptation.impl.DefaultKevoreeAdaptationFactory()
            val scheduling = SchedulingWithTopologicalOrderAlgo()
            var step = adaptationModelFactory.createParallelStep()
            var currentStep = step
            adaptionModel.setOrderedPrimitiveSet(currentStep)
            //PROCESS STOP

            scheduling.schedule(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StopInstance }, false).forEach {
                p ->
                step.addAdaptations(p)
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }
            // REMOVE BINDINGS
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt ->
                (adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveBinding ||
                adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveFragmentBinding)
            })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // REMOVE INSTANCES
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveInstance })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // REMOVE TYPES
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveType })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // REMOVE DEPLOY UNIT
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.RemoveDeployUnit })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // ADD ThirdParty
            // ADD DeployUnit
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddThirdParty })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            /*adaptionModel.getAdaptations().filter(adapt => adapt.getPrimitiveType().getName == JavaSePrimitive.AddThirdParty)
              .foreach {
              p =>
                step = KevoreeAdaptationFactory.$instance.createParallelStep
                step.addAdaptations(p)
                currentStep.setNextStep(Some(step))
                currentStep = step
            }*/
            /*
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddThirdParty })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }  */

            // START ThirdParty
            /*
            adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StartThirdParty }
                    .forEach {
                p ->
                step = adaptationModelFactory.createParallelStep()
                step.addAdaptations(p)
                currentStep.setNextStep(step)
                currentStep = step
            } */
            /*step.addAllAdaptations(adaptionModel.getAdaptations()
              .filter(adapt => adapt.getPrimitiveType().getName == JavaSePrimitive.StartThirdParty))
            if (!step.getAdaptations().isEmpty()) {
              step = KevoreeAdaptationFactory.$instance.createParallelStep
              currentStep.setNextStep(Some(step))
              currentStep = step
            }*/

            // UPDATE ThirdParty OR DeployUnit
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.UpdateDeployUnit })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // ADD DeployUnit
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddDeployUnit })


            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // ADD DeployUnit
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddType })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // ADD Instances
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddInstance })


            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // ADD Instances
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt ->
                (adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddBinding ||
                adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.AddFragmentBinding)
            })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }

            // ADD Instances
            step.addAllAdaptations(adaptionModel.getAdaptations()
                    .filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.UpdateDictionaryInstance })
            if (!step.getAdaptations().isEmpty()) {
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                currentStep = step
            }


            var oldStep = currentStep
            //PROCESS START
            scheduling.schedule(adaptionModel.getAdaptations().filter{ adapt -> adapt.getPrimitiveType()!!.getName() == JavaSePrimitive.StartInstance }, true).forEach {
                p ->
                step.addAdaptations(p)
                step = adaptationModelFactory.createParallelStep()
                currentStep.setNextStep(step)
                oldStep = currentStep
                currentStep = step
            }
            if (step.getAdaptations().isEmpty()) {
                oldStep.setNextStep(null)
            }
        } else {
            adaptionModel.setOrderedPrimitiveSet(null)
        }
        return adaptionModel
    }
}