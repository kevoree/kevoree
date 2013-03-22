package org.kevoree.kompare.sub

import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.ContainerNode
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.ComponentInstance
import org.kevoree.MBinding
import org.kevoree.kompare.JavaSePrimitive
import org.kevoree.Channel
import org.kevoree.Dictionary
import org.kevoree.Port
import java.util.HashSet
import java.util.ArrayList
import org.kevoree.framework.kaspects.ChannelAspect
import org.kevoreeAdaptation.KevoreeAdaptationFactory
import org.kevoree.framework.kaspects.TypeDefinitionAspect
import java.util.HashMap
import org.kevoree.DeployUnit
import org.kevoree.framework.kaspects.DeployUnitAspect

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/03/13
 * Time: 16:42
 */

trait Kompare2 {

    var adaptationModelFactory: KevoreeAdaptationFactory

    fun getUpdateNodeAdaptationModel(actualNode: ContainerNode, updateNode: ContainerNode): AdaptationModel {

        val alreadyProcessInstance = HashMap<String, Any>()
        val adaptationModel = adaptationModelFactory.createAdaptationModel()
        val actualRoot = actualNode.eContainer() as ContainerRoot
        val updateRoot = updateNode.eContainer() as ContainerRoot

        val actualTD: MutableSet<String> = java.util.HashSet<String>()
        val updateTD: MutableSet<String> = java.util.HashSet<String>()

        val updatedTypeDefs: MutableSet<String> = java.util.HashSet<String>()

        val actualChannels = HashSet<String>()
        val newChannels = HashSet<String>()


        //Check Node SelfUpdate
        processInstanceDictionary(actualNode, updateNode, adaptationModel, actualRoot, updateRoot)



        //Check Remove
        for(actualComponent in actualNode.getComponents()){
            val actualComponentPath = actualComponent.path()!!
            val updatedComponent = updateRoot.findByPath(actualComponentPath, javaClass<ComponentInstance>())
            if(updatedComponent == null){
                processRemoveInstance(actualComponent, adaptationModel, actualRoot, actualTD)
                for(port in actualComponent.getProvided()){
                    for(binding in port.getBindings()){
                        processRemoveMBinding(binding, adaptationModel, actualRoot)
                        processCheckRemoveChannel(binding.getHub(), adaptationModel, actualRoot, actualTD, alreadyProcessInstance)
                        actualChannels.add(binding.getHub()!!.getName())
                    }
                }
                for(port in actualComponent.getRequired()){
                    for(binding in port.getBindings()){
                        processRemoveMBinding(binding, adaptationModel, actualRoot)
                        processCheckRemoveChannel(binding.getHub(), adaptationModel, actualRoot, actualTD, alreadyProcessInstance)
                        actualChannels.add(binding.getHub()!!.getName())
                    }
                }
                alreadyProcessInstance.put(actualComponent.path(), actualComponent)
            }
        }

        for(updatedComponent in updateNode.getComponents()){
            val updatedComponentPath = updatedComponent.path()!!
            val actualComponent = actualRoot.findByPath(updatedComponentPath, javaClass<ComponentInstance>())
            if(actualComponent == null){
                processAddInstance(updatedComponent, adaptationModel, updateRoot, updateTD)
                for(port in updatedComponent.getProvided()){
                    for(binding in port.getBindings()){
                        processAddMBinding(binding, adaptationModel, actualRoot, updateRoot)
                        processCheckAddChannel(binding.getHub(), adaptationModel, actualRoot, updateRoot, updateTD, alreadyProcessInstance)
                        newChannels.add(binding.getHub()!!.getName())
                    }
                }
                for(port in updatedComponent.getRequired()){
                    for(binding in port.getBindings()){
                        processAddMBinding(binding, adaptationModel, actualRoot, updateRoot)
                        processCheckAddChannel(binding.getHub(), adaptationModel, actualRoot, updateRoot, updateTD, alreadyProcessInstance)
                        newChannels.add(binding.getHub()!!.getName())
                    }
                }
                alreadyProcessInstance.put(updatedComponent.path(), updatedComponent)
            } else {
                processCheckUpdateInstance(actualComponent, updatedComponent, adaptationModel, actualRoot, actualTD, updateTD, updateRoot, actualNode.getName(),updatedTypeDefs)
                checkBindings(actualChannels,newChannels,actualComponent.getProvided(), updatedComponent.getProvided(), adaptationModel, actualRoot, updateRoot)
            }
        }

        checkChannels(actualChannels, newChannels, adaptationModel, actualRoot, updateRoot, actualNode.getName(), actualTD, updateTD, alreadyProcessInstance,updatedTypeDefs)


        //TODO BETTER Group Search with opposite usage
        for(actualGroup in actualRoot.getGroups()){
            if(actualGroup.findSubNodesByID(actualNode.getName()) != null){
                val updateGroup = updateRoot.findGroupsByID(actualGroup.getName())
                if(updateGroup == null || updateGroup.findSubNodesByID(actualNode.getName()) == null){
                    processRemoveInstance(actualGroup, adaptationModel, actualRoot, actualTD)
                }
            }
        }
        for(updateGroup in updateRoot.getGroups()){
            if(updateGroup.findSubNodesByID(actualNode.getName()) != null){
                val actualGroup = actualRoot.findGroupsByID(updateGroup.getName())
                if(actualGroup == null || actualGroup.findSubNodesByID(actualNode.getName()) == null){
                    processAddInstance(updateGroup, adaptationModel, updateRoot, updateTD)
                } else {
                    //Check dictionary
                    processCheckUpdateInstance(actualGroup, updateGroup, adaptationModel, actualRoot, actualTD, updateTD, updateRoot, actualNode.getName(),updatedTypeDefs)
                    if(actualGroup.getSubNodes().size != updateGroup.getSubNodes().size){
                        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                        ccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
                        ccmd.setRef(updateGroup)
                        adaptationModel.addAdaptations(ccmd)
                    } else {
                        var foundAll = true
                        for(subNode in updateGroup.getSubNodes()){
                            if(actualGroup.findSubNodesByID(subNode.getName()) == null){
                                foundAll = false
                            }
                        }
                        if(!foundAll){
                            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                            ccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
                            ccmd.setRef(updateGroup)
                            adaptationModel.addAdaptations(ccmd)
                        }
                    }
                }
            }
        }
        checkTypes(actualTD, updateTD, adaptationModel, actualRoot, updateRoot, actualNode)
        return adaptationModel
    }

    fun traverseDU(du: DeployUnit, map: HashMap<String, DeployUnit>, tp: Boolean, mapTP: HashMap<String, DeployUnit>) {
        val duAspect = DeployUnitAspect()
        map.put(duAspect.buildKey(du), du)
        if(tp){
            mapTP.put(duAspect.buildKey(du), du)
        }
        for(rLib in du.getRequiredLibs()){
            traverseDU(rLib, map, true, mapTP)
        }
    }

    fun checkTypes(actualTD: MutableSet<String>, updatedTD: MutableSet<String>, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot, actualNode: ContainerNode) {
        val usefull_DU = HashMap<String, DeployUnit>()
        val useless_DU = HashMap<String, DeployUnit>()

        val tp_DU = HashMap<String, DeployUnit>()

        val potentialAdd = HashMap<String, DeployUnit>()
        val duAspect = DeployUnitAspect()
        val tdAspect = TypeDefinitionAspect()
        for(actualType in actualTD){
            if(!updatedTD.contains(actualType)){
                //Remove Type
                val td = actualRoot.findTypeDefinitionsByID(actualType)!!
                val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                ccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType))
                ccmd.setRef(td)
                adaptationModel.addAdaptations(ccmd)
                //TAG DU TO BE USELESS
                val du = tdAspect.foundRelevantDeployUnit(td, actualNode)!!
                traverseDU(du, useless_DU, false, tp_DU)
            }
        }
        val duUpdated = HashMap<String, DeployUnit>()
        for(updateType in updatedTD){
            val td = updateRoot.findTypeDefinitionsByID(updateType)!!
            if(!actualTD.contains(updateType)){
                //Add Type
                val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                ccmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType))
                ccmd.setRef(td)
                adaptationModel.addAdaptations(ccmd)
                val du = tdAspect.foundRelevantDeployUnit(td, actualNode)!!
                traverseDU(du, potentialAdd, false, tp_DU)
            } else {
                //CHECK IF TYPE IS UPDATED
                val td2 = actualRoot.findTypeDefinitionsByID(updateType)!!
                val du = tdAspect.foundRelevantDeployUnit(td, actualNode)!!
                if(tdAspect.isUpdated(td, td2)){
                    val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                    ccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateType))
                    ccmd.setRef(td)
                    adaptationModel.addAdaptations(ccmd)
                    //UPDATE DEPLOY UNIT
                    duUpdated.put(duAspect.buildKey(du), du)
                    //LOOK FOR UPDATE DEPLOY_UNIT
                }
                traverseDU(du, usefull_DU, false, tp_DU)
            }
        }

        for(du in duUpdated){
            val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
            ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDeployUnit))
            ccmd2.setRef(du.getValue())
            adaptationModel.addAdaptations(ccmd2)
        }

        for(useless_DU_elem in useless_DU){
            if(usefull_DU.get(useless_DU_elem.getKey()) == null){
                val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
                ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveDeployUnit))
                ccmd2.setRef(useless_DU_elem.getValue())
                adaptationModel.addAdaptations(ccmd2)
            }
        }
        for(potentialAddDU in potentialAdd){
            if(usefull_DU.get(potentialAddDU.getKey()) == null){
                val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()

                if(tp_DU.containsKey(potentialAddDU.key)){
                    ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddThirdParty))
                } else {
                    ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddDeployUnit))
                }
                ccmd2.setRef(potentialAddDU.getValue())
                adaptationModel.addAdaptations(ccmd2)
            }
        }

    }


    fun checkBindings(actualChannel : HashSet<String>,newChannel : HashSet<String>,actualPorts: List<Port>, updatePorts: List<Port>, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        for(aPort in actualPorts){
            for(aBinding in aPort.getBindings()){
                @beforeFoundSibling for(uPort in updatePorts){
                    if(aPort.getPortTypeRef()!!.getName() == uPort.getPortTypeRef()!!.getName()){
                        for(uBinding in uPort.getBindings()){
                            if(uBinding.getHub()!!.path() == aBinding.getHub()!!.path()){
                                break@beforeFoundSibling
                            }
                        }
                        processRemoveMBinding(aBinding, adaptationModel, actualRoot)
                    }
                }
                actualChannel.add(aBinding.getHub()!!.getName())
            }
        }
        val checkedChannels2 = HashSet<String>()
        for(uPort in updatePorts){
            for(uBinding in uPort.getBindings()){
                if(!actualChannel.contains(uBinding.getHub()!!.getName())){
                    @beforeFoundSibling for(aPort in actualPorts){
                        if(aPort.getPortTypeRef()!!.getName() == uPort.getPortTypeRef()!!.getName()){
                            for(aBinding in aPort.getBindings()){
                                if(uBinding.getHub()!!.path() == aBinding.getHub()!!.path()){
                                    break@beforeFoundSibling
                                }
                            }
                            processAddMBinding(uBinding, adaptationModel, actualRoot, updateRoot)
                        }
                    }
                }
                newChannel.add(uBinding.getHub()!!.getName())
            }
        }
    }

    fun processCheckRemoveChannel(actualChannel: Channel?, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, actualTD: MutableSet<String>, alreadyProcessInstance: HashMap<String, Any>) {
        if(alreadyProcessInstance.get(actualChannel!!.path()) != null){
            return //already checked
        }
        if(actualChannel != null){
            val updateChannel = actualRoot.findByPath(actualChannel.path(), javaClass<Channel>())
            if(updateChannel == null){
                processRemoveInstance(actualChannel, adaptationModel, actualRoot, actualTD)
                for(binding in actualChannel.getBindings()){
                    processRemoveMBinding(binding, adaptationModel, actualRoot)
                }
            }
        }
        alreadyProcessInstance.put(actualChannel!!.path(), actualChannel)
    }

    fun processCheckAddChannel(updateChannel: Channel?, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot, updateTD: MutableSet<String>, alreadyProcessInstance: HashMap<String, Any>) {
        if(alreadyProcessInstance.get(updateChannel!!.path()) != null){
            return //already checked
        }
        if(updateChannel != null){
            val actualChannel = actualRoot.findByPath(updateChannel.path(), javaClass<Channel>())
            if(actualChannel == null){
                processAddInstance(updateChannel, adaptationModel, updateRoot, updateTD)
            }
        }
        alreadyProcessInstance.put(updateChannel!!.path(), updateChannel)
    }

    fun processRemoveMBinding(actualMB: MBinding, adaptationModel: AdaptationModel, root: ContainerRoot) {
        val ctcmd = adaptationModelFactory.createAdaptationPrimitive()
        ctcmd.setPrimitiveType(root.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding))
        ctcmd.setRef(actualMB)
        adaptationModel.addAdaptations(ctcmd)
    }

    fun processAddMBinding(updated: MBinding, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        val ctcmd = adaptationModelFactory.createAdaptationPrimitive()
        ctcmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding))
        ctcmd.setRef(updated)
        adaptationModel.addAdaptations(ctcmd)
    }


    fun processRemoveInstance(actualInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, actualTD: MutableSet<String>) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance))
        ccmd.setRef(actualInstance)
        adaptationModel.addAdaptations(ccmd)

        val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance))
        ccmd2.setRef(actualInstance)
        adaptationModel.addAdaptations(ccmd2)

        actualTD.add(actualInstance.getTypeDefinition()!!.getName())
    }

    fun processAddInstance(updatedInstance: Instance, adaptationModel: AdaptationModel, updateRoot: ContainerRoot, updatedTD: MutableSet<String>) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance))
        ccmd.setRef(updatedInstance)
        adaptationModel.addAdaptations(ccmd)

        val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance))
        ccmd2.setRef(updatedInstance)
        adaptationModel.addAdaptations(ccmd2)

        val ccmd3 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd3.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
        ccmd3.setRef(updatedInstance)
        adaptationModel.addAdaptations(ccmd3)

        updatedTD.add(updatedInstance.getTypeDefinition()!!.getName())
    }

    fun processCheckUpdateInstance(actualInstance: Instance, updatedInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, actualUsedTD: MutableSet<String>, updateTD: MutableSet<String>, updateRoot: ContainerRoot, nodeName: String, updateInstances : MutableSet<String>) : Boolean {
        processInstanceDictionary(actualInstance, updatedInstance, adaptationModel, actualRoot, updateRoot)
        actualUsedTD.add(actualInstance.getTypeDefinition()!!.getName())
        updateTD.add(updatedInstance.getTypeDefinition()!!.getName())
        var isUpdated = false
        val TDAspect = TypeDefinitionAspect()
        if(TDAspect.isUpdated(actualInstance.getTypeDefinition()!!, updatedInstance.getTypeDefinition()!!)){
            isUpdated = true
            updateInstances.add(actualInstance.getName())
            if(actualInstance is Channel){
                for(binding in (actualInstance as Channel).getBindings()){
                    checkUpdateOrRemove(binding, (updatedInstance as Channel).getBindings(), adaptationModel, actualRoot, nodeName)
                }
            }
            if(actualInstance is ComponentInstance){
                for(port in (actualInstance as ComponentInstance).getProvided()){
                    val newComponentInstance = (updatedInstance as ComponentInstance)
                    var foundedPort = false
                    for(p in newComponentInstance.getProvided()){
                        if(p.getPortTypeRef()!!.getName().equals(port.getPortTypeRef()!!.getName())){
                            for(binding in port.getBindings()){
                                checkUpdateOrRemove(binding, p.getBindings(), adaptationModel, actualRoot, nodeName)
                                foundedPort = true
                            }
                        }
                    }
                    if(!foundedPort){
                        for(binding in port.getBindings()){
                            checkUpdateOrRemove(binding, ArrayList<MBinding>(), adaptationModel, actualRoot, nodeName)
                        }
                    }
                }
                for(port in (actualInstance as ComponentInstance).getRequired()){
                    val newComponentInstance = (updatedInstance as ComponentInstance)
                    var foundedPort = false
                    for(p in newComponentInstance.getRequired()){
                        if(p.getPortTypeRef()!!.getName().equals(port.getPortTypeRef()!!.getName())){
                            for(binding in port.getBindings()){
                                checkUpdateOrRemove(binding, p.getBindings(), adaptationModel, actualRoot, nodeName)
                                foundedPort = true
                            }
                        }
                    }
                    if(!foundedPort){
                        for(binding in port.getBindings()){
                            checkUpdateOrRemove(binding, ArrayList<MBinding>(), adaptationModel, actualRoot, nodeName)
                        }
                    }
                }
            }
            val ccmd3 = adaptationModelFactory.createAdaptationPrimitive()
            ccmd3.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateInstance))
            val res = array(actualInstance, updatedInstance)
            ccmd3.setRef(res)
            adaptationModel.addAdaptations(ccmd3)
        }
        return isUpdated
    }

    fun checkUpdateOrRemove(binding: MBinding, bindings: List<MBinding>, adaptationModel: AdaptationModel, model: ContainerRoot, nodeName: String) {
        if( (binding.getPort()!!.eContainer()!!.eContainer() as ContainerNode).getName() != nodeName){
            return
        }
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        var foundInNew = false
        @lookB for(newBinding in bindings){
            if(newBinding.getHub()!!.path().equals(binding.getHub()!!.path())
            && newBinding.getPort()!!.getPortTypeRef()!!.getName().equals(binding.getPort()!!.getPortTypeRef()!!.getName())
            && (newBinding.getPort()!!.eContainer()!! as ComponentInstance).getName().equals((binding.getPort()!!.eContainer()!! as ComponentInstance).getName())
            ){
                foundInNew = true
                break@lookB
            }
        }
        if(foundInNew){
            ccmd.setPrimitiveType(model.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateBinding))
        } else {
            ccmd.setPrimitiveType(model.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding))
        }
        ccmd.setRef(binding)
        adaptationModel.addAdaptations(ccmd)
    }


    fun processInstanceDictionary(actualInstance: Instance, updateInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        if(actualInstance.getDictionary() == null && updateInstance.getDictionary() != null){
            return updateDictionary(actualInstance, updateInstance, adaptationModel, actualRoot, updateRoot)
        }
        if(actualInstance.getDictionary() != null && updateInstance.getDictionary() == null){
            return updateDictionary(actualInstance, updateInstance, adaptationModel, actualRoot, updateRoot)
        }
        if(actualInstance.getDictionary() == null && updateInstance.getDictionary() == null){
            return
        }
        //TODO CACHE
        if(checkDictionary(actualInstance.getDictionary()!!, updateInstance.getDictionary()!!)){
            return updateDictionary(actualInstance, updateInstance, adaptationModel, actualRoot, updateRoot)
        }
        if(checkDictionary(updateInstance.getDictionary()!!, actualInstance.getDictionary()!!)){
            return updateDictionary(actualInstance, updateInstance, adaptationModel, actualRoot, updateRoot)
        }
    }

    fun checkDictionary(dico1: Dictionary, dico2: Dictionary): Boolean {
        for(dic1Val in dico1.getValues()){
            var checkedOk = false
            for(dic2Val in dico2.getValues()){
                if(dic1Val.getAttribute()!!.getName() == dic2Val.getAttribute()!!.getName() && dic1Val.getTargetNode() == dic2Val.getTargetNode()){
                    if(dic1Val.getValue() == dic2Val.getValue()){
                        checkedOk = true
                    } else {
                        checkedOk = false
                    }
                }
            }
            if(!checkedOk){
                return true
            }
        }
        return false
    }

    fun updateDictionary(actualInstance: Instance, updateInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
        if(ccmd.getPrimitiveType() == null){
            ccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
        }
        ccmd.setRef(updateInstance)
        adaptationModel.addAdaptations(ccmd)
    }

    fun checkChannels(actualChannelName: Set<String>, updateChannelName: Set<String>, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot, nodeName: String, actualTD: MutableSet<String>, updateTD: MutableSet<String>, alreadyChecked: HashMap<String, Any>,updatedTypeDefs : MutableSet<String>) {

        val channelAspect = ChannelAspect()
        for(ch1 in actualChannelName){
            val ch2 = updateRoot.findHubsByID(ch1)
            if(ch2 == null){
                val channelOrigin = actualRoot.findHubsByID(ch1)!!
                processCheckRemoveChannel(channelOrigin, adaptationModel, actualRoot, actualTD, alreadyChecked)
                for(remote in channelAspect.getConnectedNode(channelOrigin, nodeName)){
                    val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                    addccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding))
                    addccmd.setRef(channelOrigin)
                    addccmd.setTargetNodeName(remote.getName())
                    adaptationModel.addAdaptations(addccmd)
                }
            }
        }
        for(ch1 in updateChannelName){
            val channelOrigin = updateRoot.findHubsByID(ch1)!!
            val ch2 = actualRoot.findHubsByID(ch1)
            if(ch2 == null){
                processCheckAddChannel(updateRoot.findHubsByID(ch1), adaptationModel, actualRoot, updateRoot, updateTD, alreadyChecked)
                for(remote in channelAspect.getConnectedNode(channelOrigin, nodeName)){
                    val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                    addccmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding))
                    addccmd.setRef(channelOrigin)
                    addccmd.setTargetNodeName(remote.getName())
                    adaptationModel.addAdaptations(addccmd)
                }
            } else {
                processCheckUpdateInstance(actualRoot.findHubsByID(ch1), updateRoot.findHubsByID(ch1), adaptationModel, actualRoot, actualTD, updateTD, updateRoot, nodeName,updatedTypeDefs)
                val remotesUpdate = channelAspect.getConnectedNode(channelOrigin, nodeName)
                val remotesActual = channelAspect.getConnectedNode(updateRoot.findHubsByID(ch1), nodeName)
                for(remoteUpdate in remotesUpdate){
                    var found = false
                    @beforeFound for(remoteActual in remotesActual){
                        if(remoteUpdate.getName() == remoteActual.getName()){
                            found = true
                            break@beforeFound
                        }
                    }
                    if(!found){
                        val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                        addccmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding))
                        addccmd.setRef(channelOrigin)
                        addccmd.setTargetNodeName(remoteUpdate.getName())
                        adaptationModel.addAdaptations(addccmd)
                    }
                }
                for(remoteActual in remotesActual){
                    var found = false
                    @beforeFound for(remoteUpdate in remotesUpdate){
                        if(remoteUpdate.getName() == remoteActual.getName()){
                            found = true
                            break@beforeFound
                        }
                    }
                    if(!found){
                        val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                        addccmd.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding))
                        addccmd.setRef(channelOrigin)
                        addccmd.setTargetNodeName(remoteActual.getName())
                        adaptationModel.addAdaptations(addccmd)
                    } else {
                        //If channel update
                        if(updatedTypeDefs.contains(channelOrigin.getName())){
                            val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                            addccmd.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateFragmentBinding))
                            addccmd.setRef(channelOrigin)
                            addccmd.setTargetNodeName(remoteActual.getName())
                            adaptationModel.addAdaptations(addccmd)
                        }
                    }
                }
            }
        }

    }
}