package org.kevoree.kompare.sub

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import org.kevoree.Channel
import org.kevoree.ComponentInstance
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.DeployUnit
import org.kevoree.Dictionary
import org.kevoree.Instance
import org.kevoree.MBinding
import org.kevoree.Port
import org.kevoree.framework.kaspects.ChannelAspect
import org.kevoree.framework.kaspects.DeployUnitAspect
import org.kevoree.framework.kaspects.TypeDefinitionAspect
import org.kevoree.kompare.JavaSePrimitive
import org.kevoreeadaptation.AdaptationModel
import org.kevoreeadaptation.KevoreeAdaptationFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/03/13
 * Time: 16:42
 */

trait Kompare2 {

    var adaptationModelFactory: KevoreeAdaptationFactory

    fun getUpdateNodeAdaptationModel(actualNode: ContainerNode?, updateNode: ContainerNode?, actualRoot: ContainerRoot, updateRoot: ContainerRoot, nodeName: String): AdaptationModel {

        val alreadyProcessInstance = HashMap<String, Any>()
        val adaptationModel = adaptationModelFactory.createAdaptationModel()

        val actualTD: MutableSet<String> = java.util.HashSet<String>()
        val updateTD: MutableSet<String> = java.util.HashSet<String>()

        val updatedTypeDefs: MutableSet<String> = java.util.HashSet<String>()

        val actualChannels = HashSet<String>()
        val newChannels = HashSet<String>()

        //Check Node SelfUpdate
        processInstanceDictionary(actualNode, updateNode, adaptationModel, actualRoot, updateRoot)



        //Check Remove
        if(actualNode != null){
            for(actualComponent in actualNode.components){
                val actualComponentPath = actualComponent.path()!!
                val updatedComponent = updateRoot.findByPath(actualComponentPath, javaClass<ComponentInstance>())
                if(updatedComponent == null){
                    processRemoveInstance(actualComponent, adaptationModel, actualRoot, actualTD)
                    for(port in actualComponent.provided){
                        for(binding in port.bindings){
                            processRemoveMBinding(binding, adaptationModel, actualRoot)
                            processCheckRemoveChannel(binding.hub, adaptationModel, actualRoot, actualTD, alreadyProcessInstance)
                            actualChannels.add(binding.hub!!.name!!)
                        }
                    }
                    for(port in actualComponent.required){
                        for(binding in port.bindings){
                            processRemoveMBinding(binding, adaptationModel, actualRoot)
                            processCheckRemoveChannel(binding.hub, adaptationModel, actualRoot, actualTD, alreadyProcessInstance)
                            actualChannels.add(binding.hub!!.name!!)
                        }
                    }
                    alreadyProcessInstance.put(actualComponent.path()!!, actualComponent)
                }
            }
        }

        // Check for add and update
        if(updateNode != null){
            for(updatedComponent in updateNode.components){
                val updatedComponentPath = updatedComponent.path()!!
                val actualComponent = actualRoot.findByPath(updatedComponentPath, javaClass<ComponentInstance>())
                if(actualComponent == null){
                    processAddInstance(updatedComponent, adaptationModel, updateRoot, updateTD)
                    for(port in updatedComponent.provided){
                        for(binding in port.bindings){
                            processAddMBinding(binding, adaptationModel, actualRoot, updateRoot)
                            processCheckAddChannel(binding.hub, adaptationModel, actualRoot, updateRoot, updateTD, alreadyProcessInstance)
                            newChannels.add(binding.hub!!.name!!)
                        }
                    }
                    for(port in updatedComponent.required){
                        for(binding in port.bindings){
                            processAddMBinding(binding, adaptationModel, actualRoot, updateRoot)
                            processCheckAddChannel(binding.hub, adaptationModel, actualRoot, updateRoot, updateTD, alreadyProcessInstance)
                            newChannels.add(binding.hub!!.name!!)
                        }
                    }
                    alreadyProcessInstance.put(updatedComponent.path()!!, updatedComponent)
                } else {
                    processCheckUpdateInstance(actualComponent, updatedComponent, adaptationModel, actualRoot, actualTD, updateTD, updateRoot, nodeName, updatedTypeDefs)
                    checkBindings(actualChannels, newChannels, actualComponent.provided, updatedComponent.provided, adaptationModel, actualRoot, updateRoot)
                    checkBindings(actualChannels, newChannels, actualComponent.required, updatedComponent.required, adaptationModel, actualRoot, updateRoot)
                    processCheckStartAndStopInstance(actualComponent, updatedComponent, adaptationModel, actualRoot, updateRoot)
                }
            }
        }

        checkChannels(actualChannels, newChannels, adaptationModel, actualRoot, updateRoot, nodeName, actualTD, updateTD, alreadyProcessInstance, updatedTypeDefs)
        //TODO BETTER Group Search with opposite usage
        if(actualNode != null){
            for(actualGroup in actualRoot.groups){
                if(actualGroup.findSubNodesByID(actualNode.name) != null){
                    val updateGroup = updateRoot.findGroupsByID(actualGroup.name)
                    if(updateGroup == null || updateGroup.findSubNodesByID(actualNode.name) == null){
                        processRemoveInstance(actualGroup, adaptationModel, actualRoot, actualTD)
                    }
                }
            }
        }
        for(updateGroup in updateRoot.groups){
            if(updateGroup.findSubNodesByID(nodeName) != null){
                val actualGroup = actualRoot.findGroupsByID(updateGroup.name)
                if(actualGroup == null || actualGroup.findSubNodesByID(nodeName) == null){
                    processAddInstance(updateGroup, adaptationModel, updateRoot, updateTD)
                } else {
                    //Check dictionary
                    processCheckUpdateInstance(actualGroup, updateGroup, adaptationModel, actualRoot, actualTD, updateTD, updateRoot, nodeName, updatedTypeDefs)
                    if(actualGroup.subNodes.size != updateGroup.subNodes.size){
                        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                        ccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
                        ccmd.ref = (updateGroup)
                        adaptationModel.addAdaptations(ccmd)
                    } else {
                        var foundAll = true
                        for(subNode in updateGroup.subNodes){
                            if(actualGroup.findSubNodesByID(subNode.name) == null){
                                foundAll = false
                            }
                        }
                        if(!foundAll){
                            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                            ccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
                            ccmd.ref = (updateGroup)
                            adaptationModel.addAdaptations(ccmd)
                        }
                    }
                    processCheckStartAndStopInstance(actualGroup, updateGroup, adaptationModel, actualRoot, updateRoot)
                }
            }
        }

        var baseLookupNodeForDU = actualNode
        if(baseLookupNodeForDU == null){
            baseLookupNodeForDU = updateNode
        }
        checkTypes(actualTD, updateTD, adaptationModel, actualRoot, updateRoot, baseLookupNodeForDU!!)
        return adaptationModel
    }

    fun traverseDU(du: DeployUnit, map: HashMap<String, DeployUnit>, tp: Boolean, mapTP: HashMap<String, DeployUnit>) {
        val duAspect = DeployUnitAspect()
        map.put(duAspect.buildKey(du), du)
        if(tp){
            mapTP.put(duAspect.buildKey(du), du)
        }
        for(rLib in du.requiredLibs){
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


        traverseDU(tdAspect.foundRelevantDeployUnit(actualNode.typeDefinition!!, actualNode)!!, usefull_DU, true, tp_DU) //keep current node DU installed (no unbootstrap with kompare)

        for(actualType in actualTD){
            if(!updatedTD.contains(actualType)){
                //Remove Type
                val td = actualRoot.findTypeDefinitionsByID(actualType)!!
                val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                ccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType))
                ccmd.ref = (td)
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
                ccmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType))
                ccmd.ref = (td)
                adaptationModel.addAdaptations(ccmd)
                val du = tdAspect.foundRelevantDeployUnit(td, actualNode)!!
                traverseDU(du, potentialAdd, false, tp_DU)
            } else {
                //CHECK IF TYPE IS UPDATED
                val td2 = actualRoot.findTypeDefinitionsByID(updateType)!!
                val du = tdAspect.foundRelevantDeployUnit(td, actualNode)!!
                if(tdAspect.isUpdated(td, td2)){
                    val ccmd = adaptationModelFactory.createAdaptationPrimitive()
                    ccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateType))
                    ccmd.ref = (td)
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
            ccmd2.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDeployUnit))
            ccmd2.ref = (du.value)
            adaptationModel.addAdaptations(ccmd2)
        }

        for(useless_DU_elem in useless_DU){
            if(usefull_DU.get(useless_DU_elem.getKey()) == null){
                val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
                ccmd2.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveDeployUnit))
                ccmd2.ref = (useless_DU_elem.value)
                adaptationModel.addAdaptations(ccmd2)
            }
        }
        for(potentialAddDU in potentialAdd){
            if(usefull_DU.get(potentialAddDU.getKey()) == null){
                val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()

                if(tp_DU.containsKey(potentialAddDU.key)){
                    ccmd2.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddThirdParty))
                } else {
                    ccmd2.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddDeployUnit))
                }
                ccmd2.ref = (potentialAddDU.value)
                adaptationModel.addAdaptations(ccmd2)
            }
        }

    }


    fun checkBindings(actualChannel: HashSet<String>, newChannel: HashSet<String>, actualPorts: List<Port>, updatePorts: List<Port>, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        for(aPort in actualPorts){
            for(aBinding in aPort.bindings){
                @beforeFoundSibling for(uPort in updatePorts){
                    if(aPort.portTypeRef!!.name == uPort.portTypeRef!!.name){
                        for(uBinding in uPort.bindings){
                            if(uBinding.hub!!.path() == aBinding.hub!!.path()){
                                break@beforeFoundSibling
                            }
                        }
                        processRemoveMBinding(aBinding, adaptationModel, actualRoot)
                    }
                }
                actualChannel.add(aBinding.hub!!.name!!)
            }
        }
        val checkedChannels2 = HashSet<String>()
        for(uPort in updatePorts){
            for(uBinding in uPort.bindings){
                if(!actualChannel.contains(uBinding.hub!!.name)){
                    @beforeFoundSibling for(aPort in actualPorts){
                        if(aPort.portTypeRef!!.name == uPort.portTypeRef!!.name){
                            for(aBinding in aPort.bindings){
                                if(uBinding.hub!!.path() == aBinding.hub!!.path()){
                                    break@beforeFoundSibling
                                }
                            }
                            processAddMBinding(uBinding, adaptationModel, actualRoot, updateRoot)
                        }
                    }
                }
                newChannel.add(uBinding.hub!!.name!!)
            }
        }
    }

    fun processCheckRemoveChannel(actualChannel: Channel?, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, actualTD: MutableSet<String>, alreadyProcessInstance: HashMap<String, Any>) {
        if(alreadyProcessInstance.get(actualChannel!!.path()) != null){
            return //already checked
        }
        if(actualChannel != null){
            val updateChannel = actualRoot.findByPath(actualChannel.path()!!, javaClass<Channel>())
            if(updateChannel == null){
                processRemoveInstance(actualChannel, adaptationModel, actualRoot, actualTD)
                for(binding in actualChannel.bindings){
                    processRemoveMBinding(binding, adaptationModel, actualRoot)
                }
            }
        }
        alreadyProcessInstance.put(actualChannel!!.path()!!, actualChannel)
    }

    fun processCheckAddChannel(updateChannel: Channel?, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot, updateTD: MutableSet<String>, alreadyProcessInstance: HashMap<String, Any>) {
        if(alreadyProcessInstance.get(updateChannel!!.path()) != null){
            return //already checked
        }
        if(updateChannel != null){
            val actualChannel = actualRoot.findByPath(updateChannel.path()!!, javaClass<Channel>())
            if(actualChannel == null){
                processAddInstance(updateChannel, adaptationModel, updateRoot, updateTD)
            } else {
                processCheckStartAndStopInstance(actualChannel, updateChannel, adaptationModel, actualRoot, updateRoot)
            }
        }
        alreadyProcessInstance.put(updateChannel!!.path()!!, updateChannel)
    }

    fun processRemoveMBinding(actualMB: MBinding, adaptationModel: AdaptationModel, root: ContainerRoot) {
        val ctcmd = adaptationModelFactory.createAdaptationPrimitive()
        ctcmd.primitiveType = (root.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding))
        ctcmd.ref = (actualMB)
        adaptationModel.addAdaptations(ctcmd)
    }

    fun processAddMBinding(updated: MBinding, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        val ctcmd = adaptationModelFactory.createAdaptationPrimitive()
        ctcmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding))
        ctcmd.ref = (updated)
        adaptationModel.addAdaptations(ctcmd)
    }


    fun processRemoveInstance(actualInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, actualTD: MutableSet<String>) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance))
        ccmd.ref = (actualInstance)
        adaptationModel.addAdaptations(ccmd)

        processStopInstance(actualInstance, adaptationModel, actualRoot)

        actualTD.add(actualInstance.typeDefinition!!.name!!)
    }

    fun processStopInstance(actualInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot) {
        val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd2.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance))
        ccmd2.ref = (actualInstance)
        adaptationModel.addAdaptations(ccmd2)
    }

    fun processAddInstance(updatedInstance: Instance, adaptationModel: AdaptationModel, updateRoot: ContainerRoot, updatedTD: MutableSet<String>) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance))
        ccmd.ref = (updatedInstance)
        adaptationModel.addAdaptations(ccmd)

        if (updatedInstance.started!!) {
            processStartInstance(updatedInstance, adaptationModel, updateRoot)
        }

        val ccmd3 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd3.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
        ccmd3.ref = (updatedInstance)
        adaptationModel.addAdaptations(ccmd3)

        updatedTD.add(updatedInstance.typeDefinition!!.name!!)
    }

    fun processStartInstance(updatedInstance: Instance, adaptationModel: AdaptationModel, updateRoot: ContainerRoot) {
        val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd2.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance))
        ccmd2.ref = updatedInstance
        adaptationModel.addAdaptations(ccmd2)
    }

    fun processCheckStartAndStopInstance(actualInstance: Instance, updatedInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        if (updatedInstance.started != actualInstance.started) {
            if (actualInstance.started!!) {
                processStopInstance(actualInstance, adaptationModel, actualRoot)
            } else {
                processStartInstance(actualInstance, adaptationModel, actualRoot);
            }
        }
    }

    fun processCheckUpdateInstance(actualInstance: Instance, updatedInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, actualUsedTD: MutableSet<String>, updateTD: MutableSet<String>, updateRoot: ContainerRoot, nodeName: String, updateInstances: MutableSet<String>): Boolean {
        processInstanceDictionary(actualInstance, updatedInstance, adaptationModel, actualRoot, updateRoot)
        actualUsedTD.add(actualInstance.typeDefinition!!.name!!)
        updateTD.add(updatedInstance.typeDefinition!!.name!!)
        var isUpdated = false
        val TDAspect = TypeDefinitionAspect()
        if(TDAspect.isUpdated(actualInstance.typeDefinition!!, updatedInstance.typeDefinition!!)){
            isUpdated = true
            updateInstances.add(actualInstance.name!!)
            if(actualInstance is Channel){
                for(binding in (actualInstance as Channel).bindings){
                    checkUpdateOrRemove(binding, (updatedInstance as Channel).bindings, adaptationModel, actualRoot, nodeName)
                }
            }
            if(actualInstance is ComponentInstance){
                for(port in (actualInstance as ComponentInstance).provided){
                    val newComponentInstance = (updatedInstance as ComponentInstance)
                    var foundedPort = false
                    for(p in newComponentInstance.provided){
                        if(p.portTypeRef!!.name.equals(port.portTypeRef!!.name)){
                            for(binding in port.bindings){
                                checkUpdateOrRemove(binding, p.bindings, adaptationModel, actualRoot, nodeName)
                                foundedPort = true
                            }
                        }
                    }
                    if(!foundedPort){
                        for(binding in port.bindings){
                            checkUpdateOrRemove(binding, ArrayList<MBinding>(), adaptationModel, actualRoot, nodeName)
                        }
                    }
                }
                for(port in (actualInstance as ComponentInstance).required){
                    val newComponentInstance = (updatedInstance as ComponentInstance)
                    var foundedPort = false
                    for(p in newComponentInstance.required){
                        if(p.portTypeRef!!.name.equals(port.portTypeRef!!.name)){
                            for(binding in port.bindings){
                                checkUpdateOrRemove(binding, p.bindings, adaptationModel, actualRoot, nodeName)
                                foundedPort = true
                            }
                        }
                    }
                    if(!foundedPort){
                        for(binding in port.bindings){
                            checkUpdateOrRemove(binding, ArrayList<MBinding>(), adaptationModel, actualRoot, nodeName)
                        }
                    }
                }
            }
            val ccmd3 = adaptationModelFactory.createAdaptationPrimitive()
            ccmd3.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateInstance))
            val res = array(actualInstance, updatedInstance)
            ccmd3.ref = (res)
            adaptationModel.addAdaptations(ccmd3)
        }
        return isUpdated
    }

    fun checkUpdateOrRemove(binding: MBinding, bindings: List<MBinding>, adaptationModel: AdaptationModel, model: ContainerRoot, nodeName: String) {
        if( (binding.port!!.eContainer()!!.eContainer() as ContainerNode).name != nodeName){
            return
        }
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        var foundInNew = false
        @lookB for(newBinding in bindings){
            if(newBinding.hub!!.path().equals(binding.hub!!.path())
            && newBinding.port!!.portTypeRef!!.name.equals(binding.port!!.portTypeRef!!.name)
            && (newBinding.port!!.eContainer()!! as ComponentInstance).name.equals((binding.port!!.eContainer()!! as ComponentInstance).name)
            ){
                foundInNew = true
                break@lookB
            }
        }
        if(foundInNew){
            ccmd.primitiveType = (model.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateBinding))
        } else {
            ccmd.primitiveType = (model.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding))
        }
        ccmd.ref = (binding)
        adaptationModel.addAdaptations(ccmd)
    }


    fun processInstanceDictionary(actualInstance: Instance?, updateInstance: Instance?, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        if(actualInstance == null && updateInstance == null){
            return
        }
        if(actualInstance == null || updateInstance == null){
            return
        }
        if(actualInstance.dictionary == null && updateInstance.dictionary != null){
            return updateDictionary(updateInstance, adaptationModel, actualRoot, updateRoot)
        }
        if(actualInstance.dictionary != null && updateInstance.dictionary == null){
            return updateDictionary(updateInstance, adaptationModel, actualRoot, updateRoot)
        }
        if(actualInstance.dictionary == null && updateInstance.dictionary == null){
            return
        }
        //TODO CACHE
        if(checkDictionary(actualInstance.dictionary!!, updateInstance.dictionary!!)){
            return updateDictionary(updateInstance, adaptationModel, actualRoot, updateRoot)
        }
        if(checkDictionary(updateInstance.dictionary!!, actualInstance.dictionary!!)){
            return updateDictionary(updateInstance, adaptationModel, actualRoot, updateRoot)
        }

    }

    fun checkDictionary(dico1: Dictionary, dico2: Dictionary): Boolean {
        for(dic1Val in dico1.values){
            var checkedOk = false
            for(dic2Val in dico2.values){
                if(dic1Val.attribute!!.name == dic2Val.attribute!!.name && dic1Val.targetNode?.name == dic2Val.targetNode?.name){
                    if(dic1Val.value == dic2Val.value){
                        checkedOk = true
                    } else {
                        checkedOk = false
                    }
                }
                dic2Val.targetNode
            }
            if(!checkedOk){
                return true
            }
        }
        return false
    }

    fun updateDictionary(updateInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
        if(ccmd.primitiveType == null){
            ccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
        }
        ccmd.ref = (updateInstance)
        adaptationModel.addAdaptations(ccmd)
    }

    fun checkChannels(actualChannelName: Set<String>, updateChannelName: Set<String>, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, updateRoot: ContainerRoot, nodeName: String, actualTD: MutableSet<String>, updateTD: MutableSet<String>, alreadyChecked: HashMap<String, Any>, updatedTypeDefs: MutableSet<String>) {

        val channelAspect = ChannelAspect()
        for(ch1 in actualChannelName){
            val ch2 = updateRoot.findHubsByID(ch1)
            if(ch2 == null){
                val channelOrigin = actualRoot.findHubsByID(ch1)!!
                processCheckRemoveChannel(channelOrigin, adaptationModel, actualRoot, actualTD, alreadyChecked)
                for(remote in channelAspect.getConnectedNode(channelOrigin, nodeName)){
                    val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                    addccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding))
                    addccmd.ref = channelOrigin
                    addccmd.targetNodeName = remote.name
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
                    addccmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding))
                    addccmd.ref = channelOrigin
                    addccmd.targetNodeName = remote.name
                    adaptationModel.addAdaptations(addccmd)
                }
            } else {
                if(channelAspect.usedByNode(channelOrigin, nodeName) && !channelAspect.usedByNode(ch2, nodeName)){
                    processAddInstance(ch2, adaptationModel, updateRoot, updateTD)
                    for(remote in channelAspect.getConnectedNode(channelOrigin, nodeName)){
                        val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                        addccmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding))
                        addccmd.ref = channelOrigin
                        addccmd.targetNodeName = remote.name
                        adaptationModel.addAdaptations(addccmd)
                    }
                } else {
                    processCheckStartAndStopInstance(ch2, channelOrigin, adaptationModel, actualRoot, updateRoot)
                }


                processCheckUpdateInstance(actualRoot.findHubsByID(ch1)!!, updateRoot.findHubsByID(ch1)!!, adaptationModel, actualRoot, actualTD, updateTD, updateRoot, nodeName, updatedTypeDefs)
                val remotesUpdate = channelAspect.getConnectedNode(channelOrigin, nodeName)
                val remotesActual = channelAspect.getConnectedNode(updateRoot.findHubsByID(ch1)!!, nodeName)
                for(remoteUpdate in remotesUpdate){
                    var found = false
                    @beforeFound for(remoteActual in remotesActual){
                        if(remoteUpdate.name == remoteActual.name){
                            found = true
                            break@beforeFound
                        }
                    }
                    if(!found){
                        val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                        addccmd.primitiveType = updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding)
                        addccmd.ref = channelOrigin
                        addccmd.targetNodeName = remoteUpdate.name
                        adaptationModel.addAdaptations(addccmd)
                    }
                }
                for(remoteActual in remotesActual){
                    var found = false
                    @beforeFound for(remoteUpdate in remotesUpdate){
                        if(remoteUpdate.name == remoteActual.name){
                            found = true
                            break@beforeFound
                        }
                    }
                    if(!found){
                        val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                        addccmd.primitiveType = (actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding))
                        addccmd.ref = channelOrigin
                        addccmd.targetNodeName = remoteActual.name
                        adaptationModel.addAdaptations(addccmd)
                    } else {
                        //If channel update
                        if(updatedTypeDefs.contains(channelOrigin.name)){
                            val addccmd = adaptationModelFactory.createAdaptationPrimitive()
                            addccmd.primitiveType = (updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateFragmentBinding))
                            addccmd.ref = channelOrigin
                            addccmd.targetNodeName = remoteActual.name
                            adaptationModel.addAdaptations(addccmd)
                        }
                    }
                }
            }
        }

    }
}