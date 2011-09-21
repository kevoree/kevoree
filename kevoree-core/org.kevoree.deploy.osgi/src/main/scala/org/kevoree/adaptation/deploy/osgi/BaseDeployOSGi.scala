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

package org.kevoree.adaptation.deploy.osgi

import org.kevoree.adaptation.deploy.osgi.command._
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree._
import org.osgi.framework.Bundle

//import org.kevoree.adaptation.deploy.osgi.scheduling.ChocoScheduling

import org.kevoree.adaptation.deploy.osgi.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoreeAdaptation.AdaptationModel
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

class BaseDeployOSGi(bundle : Bundle) {

	var ctx: KevoreeDeployManager = new KevoreeDeployManager
  ctx.setBundle(bundle)

	var logger = LoggerFactory.getLogger(this.getClass);

	def deploy(model: AdaptationModel, nodeName: String) = {
		if (!model.getAdaptations.isEmpty) {
			execute(schedule(buildCommandLists(model, nodeName)))
		} else {
			true
		}
	}

	def buildCommandLists(model: AdaptationModel, nodeName: String): scala.collection.mutable.Map[String, List[PrimitiveCommand]] = {
		var executedCommandTP: List[PrimitiveCommand] = List()
		//DEPLOY UNIT COMMAND
		var command_add_deployUnit: List[PrimitiveCommand] = List()
		var command_remove_deployUnit: List[PrimitiveCommand] = List()

		//TYPE LIST
		var command_add_type: List[PrimitiveCommand] = List()
		var command_remove_type: List[PrimitiveCommand] = List()

		//INSTANCE LIST
		var command_add_instance: List[PrimitiveCommand] = List()
		var command_remove_instance: List[PrimitiveCommand] = List()

		//BINDING LIST
		var command_remove_binding: List[PrimitiveCommand] = List()
		var command_add_binding: List[PrimitiveCommand] = List()

		//Life cycle command COMMAND
		var stopCommand: List[LifeCycleCommand] = List()
		var startCommand: List[LifeCycleCommand] = List()

		var updateDictionaryCommand: List[PrimitiveCommand] = List()

		model.getAdaptations.toList.foreach {
			p => p.getPrimitiveType.getName match {

				//DEPLOY UNIT CRUD ( TYPE PROVISIONNING )
				//ThirdParty CRUD
				case JavaSePrimitive.AddDeployUnit => command_add_deployUnit = command_add_deployUnit ++ List(AddDeployUnitAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx))
				case JavaSePrimitive.RemoveDeployUnit => command_remove_deployUnit = command_remove_deployUnit ++ List(RemoveDeployUnitCommand(p.getRef.asInstanceOf[DeployUnit], ctx))
				//UPDATE US MAPPED ON REMOVE INSTALL
				case JavaSePrimitive.UpdateDeployUnit => {
					command_remove_deployUnit = command_remove_deployUnit ++ List(RemoveDeployUnitCommand(p.getRef.asInstanceOf[DeployUnit], ctx))
					command_add_deployUnit = command_add_deployUnit ++ List(AddDeployUnitAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx))
				}

				//ThirdParty CRUD
				case JavaSePrimitive.AddThirdParty => executedCommandTP = executedCommandTP ++ List(AddThirdPartyAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx))
				case JavaSePrimitive.RemoveThirdParty => executedCommandTP = executedCommandTP ++ List(RemoveThirdPartyCommand(p.getRef.asInstanceOf[DeployUnit], ctx))

				//Type CRUD
				case JavaSePrimitive.AddType => command_add_type = command_add_type ++ List(AddTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName))
				case JavaSePrimitive.RemoveType => command_remove_type = command_remove_type ++ List(RemoveTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName))
				case JavaSePrimitive.UpdateType => {
					//UPDATE IS MAPPED UN REMOVE & INSTALL
					command_remove_type = command_remove_type ++ List(RemoveTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName))
					command_add_type = command_add_type ++ List(AddTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName))
				}
				//Instance CRUD
				case JavaSePrimitive.AddInstance => {
					command_add_instance = command_add_instance ++ List(AddInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					// if(ca.getRef.isInstanceOf[ComponentInstance]){
					startCommand = startCommand ++ List(StartInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					// }
				}
				case JavaSePrimitive.RemoveInstance => {
					command_remove_instance = command_remove_instance ++ List(RemoveInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					//if(ca.getRef.isInstanceOf[ComponentInstance]){
					stopCommand = stopCommand ++ List(StopInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					//}
				}
				case JavaSePrimitive.UpdateInstance => {
					//STOP & REMOVE
					command_remove_instance = command_remove_instance ++ List(RemoveInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					//if(ca.getRef.isInstanceOf[ComponentInstance]){
					stopCommand = stopCommand ++ List(StopInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					startCommand = startCommand ++ List(StartInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					//}
					command_add_instance = command_add_instance ++ List(AddInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
				}
				case JavaSePrimitive.UpdateDictionaryInstance => {
					updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
				}

				//Binding CRUD
				case JavaSePrimitive.AddBinding => command_add_binding = command_add_binding ++ List(AddBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName))
				case JavaSePrimitive.RemoveBinding => command_remove_binding = command_remove_binding ++ List(RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName))
				case JavaSePrimitive.UpdateBinding => {
					//UPDATE MAP ON REMOVE & INSTALL
					command_add_binding = command_add_binding ++ List(AddBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName))
					command_remove_binding = command_remove_binding ++ List(RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName))
				}

				//Channel binding
				case JavaSePrimitive.AddFragmentBinding => command_add_binding = command_add_binding ++ List(AddFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, ctx, nodeName))
				case JavaSePrimitive.RemoveFragmentBinding => command_remove_binding = command_remove_binding ++ List(RemoveFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, ctx, nodeName))

				case _ => logger.error("Unknown Kevoree adaptation primitive"); false
			}
		}

		val result: scala.collection.mutable.Map[String, List[PrimitiveCommand]] = scala.collection.mutable.Map[String, List[PrimitiveCommand]]()
		result.put("stop", stopCommand)
		result.put("start", startCommand)
		result.put("addDeployUnit", command_add_deployUnit)
		result.put("removeDeployUnit", command_remove_deployUnit)
		result.put("addType", command_add_type)
		result.put("removeType", command_remove_type)
		result.put("addInstance", command_add_instance)
		result.put("removeInstance", command_remove_instance)
		result.put("removeBinding", command_remove_binding)
		result.put("addBinding", command_add_binding)
		result.put("updateDictionary", updateDictionaryCommand)
		result.put("thirdParty", executedCommandTP)

		result
	}

	def schedule(commands: scala.collection.mutable.Map[String, List[PrimitiveCommand]]): scala.collection.mutable.Map[String, List[PrimitiveCommand]] = {
		// scheduling of start and stop commands
		val initTime = System.currentTimeMillis
		val scheduling = new SchedulingWithTopologicalOrderAlgo

		commands.put("stop", scheduling.schedule(commands.get("stop").get.asInstanceOf[List[LifeCycleCommand]], false).toList)
		//stopCommand = scheduling.schedule(commands.get("stop"), false).toList
		commands.put("start", scheduling.schedule(commands.get("start").get.asInstanceOf[List[LifeCycleCommand]], true).toList)
		//startCommand = scheduling.schedule(commands.get("start"), true).toList

		val planTime = System.currentTimeMillis

		logger.debug("Plannification time = " + (planTime - initTime) + " ms");

		logger.debug("stop command: ")
		commands.get("stop").get.asInstanceOf[List[LifeCycleCommand]].foreach {
			c =>
				logger.debug(c.getInstance.getName)
		}

		logger.debug("start commands: ")
		commands.get("start").get.asInstanceOf[List[LifeCycleCommand]].foreach {
			c =>
				logger.debug(c.getInstance.getName)
		}

		commands
	}

	def execute(commands: scala.collection.mutable.Map[String, List[PrimitiveCommand]]) = {
		val initTime = System.currentTimeMillis

		val phase = new KevoreeDeployPhase(ctx)
		var executionResult = true

    // STOP INSTANCES
		if (executionResult) {
			executionResult = phase.phase(commands.get("stop").get, "Phase 0 STOP COMPONENT")
		}
    // REMOVE BINDINGS
		if (executionResult) {
			executionResult = phase.phase(commands.get("removeBinding").get, "Phase 1 Remove Binding")
		}
    // REMOVE INSTANCES
		if (executionResult) {
			executionResult = phase.phase(commands.get("removeInstance").get, "Phase 2 Remove Instance")
		}
    // REMOVE TYPES
		if (executionResult) {
			executionResult = phase.phase(commands.get("removeType").get, "Phase 3 Remove ComponentType")
		}
		if (executionResult) {
			executionResult = phase.phase(commands.get("removeDeployUnit").get, "Phase 4 Remove TypeDefinition DeployUnit")
		}
		//INSTALL TYPES
		if (executionResult) {
			executionResult = phase.phase(commands.get("thirdParty").get, "Phase 5 ThirdParty")
		}
		if (executionResult) {
			executionResult = phase.phase(commands.get("addDeployUnit").get, "Phase 6 Add TypeDefinition DeployUnit")
		}
		if (executionResult) {
			executionResult = phase.phase(commands.get("addType").get, "Phase 7 Add ComponentType")
		}
		//INSTALL INSTANCES
		if (executionResult) {
			executionResult = phase.phase(commands.get("addInstance").get, "Phase 8 install ComponentInstance")
		}
    // INSTALL BINDINGS
		if (executionResult) {
			executionResult = phase.phase(commands.get("addBinding").get, "Phase 9 install Bindings")
		}
    // UPDATE PROPERTIES
		if (executionResult) {
			executionResult = phase.phase(commands.get("updateDictionary").get, "Phase 10 SET PROPERTY")
		}
    // START INSTANCES
		if (executionResult) {
			executionResult = phase.phase(commands.get("start").get, "Phase 11 START COMPONENT")
		}

		if (!executionResult) {
			phase.rollback()
      ctx.garbage()
		}

		val deployTime = System.currentTimeMillis
    logger.debug("execution time = " + (deployTime - initTime) + " ms");
		executionResult
	}

}
