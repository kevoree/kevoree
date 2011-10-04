/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.kompare

 import scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoreeAdaptation.{KevoreeAdaptationFactory, AdaptationModel}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/09/11
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */

trait KevoreeScheduler {

  def plan(adaptionModel: AdaptationModel, nodeName: String): AdaptationModel = {
    var currentStep = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    adaptionModel.setOrderedPrimitiveSet(currentStep)

    val scheduling = new SchedulingWithTopologicalOrderAlgo
    //PROCESS STOP
    scheduling.schedule(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.StopInstance).toList, false).foreach {
      p =>
        val step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
        step.getAdaptations.add(p)
        currentStep.setNextStep(step)
        currentStep = step
    }
    // REMOVE BINDINGS
    var step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => ( adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveBinding || adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveFragmentBinding  ) ))
    currentStep.setNextStep(step)
    currentStep = step

    // REMOVE INSTANCES
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveInstance))
    currentStep.setNextStep(step)
    currentStep = step

    // REMOVE TYPES
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveType))
    currentStep.setNextStep(step)
    currentStep = step

    // REMOVE DEPLOY UNIT
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveDeployUnit))
    currentStep.setNextStep(step)
    currentStep = step

    // ADD ThirdParty
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddThirdParty))
    currentStep.setNextStep(step)
    currentStep = step

    // ADD DeployUnit
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit))
    currentStep.setNextStep(step)
    currentStep = step

    // ADD DeployUnit
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddType))
    currentStep.setNextStep(step)
    currentStep = step

    // ADD Instances
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddInstance))
    currentStep.setNextStep(step)
    currentStep = step

    // ADD Instances
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => ( adapt.getPrimitiveType.getName == JavaSePrimitive.AddBinding || adapt.getPrimitiveType.getName == JavaSePrimitive.AddFragmentBinding  ) ))
    currentStep.setNextStep(step)
    currentStep = step

    // ADD Instances
    step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
    step.getAdaptations.addAll(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.UpdateDictionaryInstance))
    currentStep.setNextStep(step)
    currentStep = step


    //PROCESS START
    scheduling.schedule(adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.StartInstance).toList, true).foreach {
      p =>
        val step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep()
        step.getAdaptations.add(p)
        currentStep.setNextStep(step)
        currentStep = step
    }
    adaptionModel
  }


}