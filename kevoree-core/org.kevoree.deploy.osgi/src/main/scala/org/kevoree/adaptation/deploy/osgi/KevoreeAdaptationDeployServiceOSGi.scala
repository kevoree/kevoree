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

import org.kevoree.ComponentInstance
import org.kevoree.adaptation.deploy.osgi.command.AddFragmentBindingCommand
import org.kevoree.adaptation.deploy.osgi.command._
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService
import org.kevoreeAdaptation.AdaptationModel
import org.kevoreeAdaptation.AddBinding
import org.kevoreeAdaptation.AddDeployUnit
import org.kevoreeAdaptation.AddFragmentBinding
import org.kevoreeAdaptation.AddInstance
import org.kevoreeAdaptation.AddType
import org.kevoreeAdaptation.AddThirdParty
import org.kevoreeAdaptation.RemoveBinding
import org.kevoreeAdaptation.RemoveDeployUnit
import org.kevoreeAdaptation.RemoveFragmentBinding
import org.kevoreeAdaptation.RemoveInstance
import org.kevoreeAdaptation.RemoveType
import org.kevoreeAdaptation.RemoveThirdParty
import org.kevoreeAdaptation.UpdateBinding
import org.kevoreeAdaptation.UpdateDeployUnit
import org.kevoreeAdaptation.UpdateDictionaryInstance
import org.kevoreeAdaptation.UpdateInstance
import org.kevoreeAdaptation.UpdateType
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

class KevoreeAdaptationDeployServiceOSGi extends KevoreeAdaptationDeployService {

  var ctx : KevoreeDeployManager = null
  var logger = LoggerFactory.getLogger(this.getClass);

  def setContext(context : KevoreeDeployManager) = { ctx = context }
  def deploy(model : AdaptationModel,nodeName:String) = {

    var phase = new KevoreeDeployPhase

    var executedCommandTP :List[PrimitiveCommand] = List()
    //var executedCommandCT :List[PrimitiveCommand] = List()
    //var executedCommandCI :List[PrimitiveCommand] = List()
    //  var executedCommandBI :List[PrimitiveCommand] = List()

    //DEPLOY UNIT COMMAND
    var command_add_deployUnit :List[PrimitiveCommand] = List()
    var command_remove_deployUnit :List[PrimitiveCommand] = List()

    //TYPE LIST
    var command_add_type :List[PrimitiveCommand] = List()
    var command_remove_type :List[PrimitiveCommand] = List()

    //INSTANCE LIST
    var command_add_instance :List[PrimitiveCommand] = List()
    var command_remove_instance :List[PrimitiveCommand] = List()

    //BINDING LIST
    var command_remove_binding :List[PrimitiveCommand] = List()
    var command_add_binding :List[PrimitiveCommand] = List()

    //Life cycle command COMMAND
    var stopCommand :List[PrimitiveCommand] = List()
    var startCommand :List[PrimitiveCommand] = List()

    var updateDictionaryCommand :List[PrimitiveCommand] = List()

    // var listPrimitive = plan(model)
    //println("plansize="+listPrimitive.size);
    model.getAdaptations foreach{ p => p match {

        //DEPLOY UNIT CRUD ( TYPE PROVISIONNING )
        //ThirdParty CRUD
        case tpa : AddDeployUnit =>command_add_deployUnit = command_add_deployUnit ++ List(AddDeployUnitCommand(tpa.getRef,ctx))
        case tpa : RemoveDeployUnit =>command_remove_deployUnit = command_remove_deployUnit ++ List(RemoveDeployUnitCommand(tpa.getRef,ctx))
          //UPDATE US MAPPED ON REMOVE INSTALL
        case tpa : UpdateDeployUnit =>{
            command_remove_deployUnit = command_remove_deployUnit ++ List(RemoveDeployUnitCommand(tpa.getRef,ctx))
            command_add_deployUnit = command_add_deployUnit ++ List(AddDeployUnitCommand(tpa.getRef,ctx))
          }

          //ThirdParty CRUD
        case tpa : AddThirdParty =>executedCommandTP = executedCommandTP ++ List(AddThirdPartyCommand(tpa.getRef,ctx))
        case tpa : RemoveThirdParty =>executedCommandTP = executedCommandTP ++ List(RemoveThirdPartyCommand(tpa.getRef,ctx))

          //Type CRUD
        case cta : AddType =>command_add_type = command_add_type ++ List(AddTypeCommand(cta.getRef,ctx))
        case cta : RemoveType =>command_remove_type = command_remove_type ++ List(RemoveTypeCommand(cta.getRef,ctx))
        case cta : UpdateType => {
            //UPDATE IS MAPPED UN REMOVE & INSTALL
            command_remove_type = command_remove_type ++ List(RemoveTypeCommand(cta.getRef,ctx))
            command_add_type = command_add_type ++ List(AddTypeCommand(cta.getRef,ctx))
          }
          //Instance CRUD
        case ca : AddInstance => {
            command_add_instance = command_add_instance ++ List(AddInstanceCommand(ca.getRef,ctx,nodeName))
            updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(ca.getRef,ctx,nodeName))
           // if(ca.getRef.isInstanceOf[ComponentInstance]){
              startCommand = startCommand ++ List(StartInstanceCommand(ca.getRef,ctx,nodeName))
           // }
          }
        case ca : RemoveInstance =>{
            command_remove_instance = command_remove_instance ++ List(RemoveInstanceCommand(ca.getRef,ctx,nodeName))
            //if(ca.getRef.isInstanceOf[ComponentInstance]){
              stopCommand = stopCommand ++ List(StopInstanceCommand(ca.getRef,ctx,nodeName))
            //}
          }
        case ca : UpdateInstance => {
            //STOP & REMOVE
            command_remove_instance = command_remove_instance ++ List(RemoveInstanceCommand(ca.getRef,ctx,nodeName))
            //if(ca.getRef.isInstanceOf[ComponentInstance]){
              stopCommand = stopCommand ++ List(StopInstanceCommand(ca.getRef,ctx,nodeName))
              startCommand = startCommand ++ List(StartInstanceCommand(ca.getRef,ctx,nodeName))
            //}
            command_add_instance = command_add_instance ++ List(AddInstanceCommand(ca.getRef,ctx,nodeName))
            updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(ca.getRef,ctx,nodeName))
          }
        case ca : UpdateDictionaryInstance => {
            updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(ca.getRef,ctx,nodeName))
          }

          //Binding CRUD
        case ca : AddBinding =>command_add_binding = command_add_binding ++ List(AddBindingCommand(ca.getRef,ctx,nodeName))
        case ca : RemoveBinding =>command_remove_binding = command_remove_binding ++ List(RemoveBindingCommand(ca.getRef,ctx,nodeName))
        case ca : UpdateBinding => {
            //UPDATE MAP ON REMOVE & INSTALL
            command_add_binding = command_add_binding ++ List(AddBindingCommand(ca.getRef,ctx,nodeName))
            command_remove_binding = command_remove_binding ++ List(RemoveBindingCommand(ca.getRef,ctx,nodeName))
        }

          //Channel binding
        case ca : AddFragmentBinding =>command_add_binding = command_add_binding ++ List(AddFragmentBindingCommand(ca.getRef,ca.getTargetNodeName,ctx,nodeName))
        case ca : RemoveFragmentBinding =>command_remove_binding = command_remove_binding ++ List(RemoveFragmentBindingCommand(ca.getRef,ca.getTargetNodeName,ctx,nodeName))

        case _ => logger.error("Unknow Kevoree adaptation primitive");false
      }
    }

    var executionResult = true

    if(executionResult){ executionResult=phase.phase(stopCommand,"Phase 0 STOP COMPONENT") }
    if(executionResult){ executionResult=phase.phase(command_remove_binding,"Phase 1 Remove Binding") }
    if(executionResult){ executionResult=phase.phase(command_remove_instance,"Phase 2 Remove Instance") }
    if(executionResult){ executionResult=phase.phase(command_remove_type,"Phase 3 Remove ComponentType") }
    if(executionResult){ executionResult=phase.phase(command_remove_deployUnit,"Phase 4 Remove TypeDefinition DeployUnit") }

    //INSTALL TYPE
    if(executionResult){ executionResult=phase.phase(executedCommandTP,"Phase 5 ThirdParty") }
    if(executionResult){ executionResult=phase.phase(command_add_deployUnit,"Phase 6 Add TypeDefinition DeployUnit") }
    if(executionResult){ executionResult=phase.phase(command_add_type,"Phase 7 Add ComponentType") }

    //INSTALL ISTANCE
    if(executionResult){ executionResult=phase.phase(command_add_instance,"Phase 8 install ComponentInstance") }
    if(executionResult){ executionResult=phase.phase(command_add_binding,"Phase 9 install Bindings") }
    if(executionResult){ executionResult=phase.phase(updateDictionaryCommand,"Phase 10 SET PROPERTY") }
    if(executionResult){ executionResult=phase.phase(startCommand,"Phase 11 START COMPONENT") }

    if(!executionResult){phase.rollback}

    executionResult
  }


  /* Simple plan algorithme / separe primitive type */
  /*
   def plan(model : AdaptationModel) : List[AdaptationPrimitive] = {
   var thirdPartiesAdaptations = model.getAdaptations.filter({a => a.isInstanceOf[ThirdPartyAdaptation] }).toList
   var componentTypeAdaptations = model.getAdaptations.filter({a => a.isInstanceOf[TypeAdaptation] }).toList
   var componentInstanceAdaptations = model.getAdaptations.filter({a => a.isInstanceOf[InstanceAdaptation] }).toList
   var bindingAdaptations = model.getAdaptations.filter({a => a.isInstanceOf[BindingAdaptation] }).toList
   var res = thirdPartiesAdaptations ++ componentTypeAdaptations ++ componentInstanceAdaptations ++ bindingAdaptations
   res.toList
   }*/
  

}
