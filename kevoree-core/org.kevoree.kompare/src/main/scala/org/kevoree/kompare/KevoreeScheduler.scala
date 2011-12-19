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
import org.kevoreeAdaptation.{ParallelStep, KevoreeAdaptationFactory, AdaptationModel}
import org.kevoree.{NamedElement, DeployUnit}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/09/11
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */

trait KevoreeScheduler {

  def plan (adaptionModel: AdaptationModel, nodeName: String): AdaptationModel = {
    if (!adaptionModel.getAdaptations.isEmpty) {

      val scheduling = new SchedulingWithTopologicalOrderAlgo
      var step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
      var currentStep = step
      adaptionModel.setOrderedPrimitiveSet(Some(currentStep))
      //PROCESS STOP
      scheduling.schedule(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.StopInstance).toList, false).foreach {
        p =>
          step.addAdaptations(p)
          step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
          currentStep.setNextStep(Some(step))
          currentStep = step
      }
      // REMOVE BINDINGS
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => (adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveBinding ||
        adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveFragmentBinding)))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // REMOVE INSTANCES
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveInstance))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // REMOVE TYPES
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveType))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // REMOVE DEPLOY UNIT
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.RemoveDeployUnit))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // ADD ThirdParty
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddThirdParty))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // START ThirdParty
      adaptionModel.getAdaptations.filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.StartThirdParty)
        .foreach {
        p =>
          step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
          step.addAdaptations(p)
          currentStep.setNextStep(Some(step))
          currentStep = step
      }
      /*step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.StartThirdParty))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }*/

      // UPDATE ThirdParty OR DeployUnit
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.UpdateDeployUnit))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // ADD DeployUnit
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // ADD DeployUnit
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddType))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // ADD Instances
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.AddInstance))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // ADD Instances
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => (adapt.getPrimitiveType.getName == JavaSePrimitive.AddBinding ||
        adapt.getPrimitiveType.getName == JavaSePrimitive.AddFragmentBinding)))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }

      // ADD Instances
      step.addAllAdaptations(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.UpdateDictionaryInstance))
      if (!step.getAdaptations.isEmpty) {
        step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
        currentStep.setNextStep(Some(step))
        currentStep = step
      }


      var oldStep = currentStep
      //PROCESS START
      scheduling.schedule(adaptionModel.getAdaptations
        .filter(adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.StartInstance).toList, true).foreach {
        p =>
          step.addAdaptations(p)
          step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep
          currentStep.setNextStep(Some(step))
          oldStep = currentStep
          currentStep = step
      }

      if (step.getAdaptations.isEmpty) {
        oldStep.setNextStep(None)
      }
    } else {
      adaptionModel.setOrderedPrimitiveSet(None)
    }
    adaptionModel
  }
}