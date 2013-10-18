package org.kevoree.kompare

import org.kevoree.ContainerRoot
import org.kevoreeadaptation.AdaptationModel
import org.kevoree.compare.DefaultModelCompare
import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory
import org.kevoree.modeling.api.trace.ModelAddTrace
import org.kevoree.modeling.api.trace.ModelSetTrace
import org.kevoree.modeling.api.trace.ModelRemoveTrace
import org.kevoree.Group
import org.kevoree.Channel
import org.kevoree.ContainerNode
import org.kevoree.ComponentInstance
import org.kevoree.modeling.api.trace.ModelAddAllTrace
import org.kevoree.modeling.api.trace.ModelRemoveAllTrace
import org.kevoree.Instance
import org.kevoree.TypeDefinition
import org.kevoree.MBinding
import java.util.ArrayList
import org.kevoree.DeployUnit
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.KMFContainer
import java.util.HashSet

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/10/13
 * Time: 17:03
 *
 * @author Erwan Daubert
 * @version 1.0
 */

open public class Kompare3(_registry : Map<String, Any>?) {

    private val modelCompare = DefaultModelCompare()
    private val adaptationModelFactory = DefaultKevoreeAdaptationFactory()

    var registry: Map<String, Any>? = _registry

    private val updatedInstances = ArrayList<Instance>()

    open public fun compareModels(currentModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {
        updatedInstances.clear()

        val currentNode = currentModel.findNodesByID(nodeName)
        val targetNode = targetModel.findNodesByID(nodeName)

        val adaptationModel = adaptationModelFactory.createAdaptationModel()
        if (currentNode != null && targetNode != null) {
            val traces = modelCompare.diff(currentNode, targetNode)
            traces.traces.forEach {
                trace ->
                System.out.println(trace)
                if (trace is ModelAddTrace) {
                    manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelRemoveTrace) {
                    manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelSetTrace) {
                    manageModelSetTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelAddAllTrace) {
                    //                    System.out.println(trace);
                } else if (trace is ModelRemoveAllTrace) {
                    //                    System.out.println(trace);
                }
            }
            dropChannelAndTypeDefinitionAndDeployUnit(currentNode, currentModel, targetNode, targetModel, adaptationModel)
        } else {
            System.err.println("One of the model doesn't have the local node")
        }
        return adaptationModel
    }

    fun manageModelAddTrace(trace: ModelAddTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
//                System.out.println(trace);
        if ((trace.typeName != null && (trace.typeName.equalsIgnoreCase(javaClass<Group>().getName())
        || trace.typeName.equals(javaClass<Channel>().getName())
        || trace.typeName.equals(javaClass<ComponentInstance>().getName())
        || trace.typeName.equals(javaClass<ContainerNode>().getName())))) {
            // Add instance
            addInstance(targetModel.findByPath(trace.previousPath!!, javaClass<Instance>())!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        } else if (trace.refName.equals("bindings")) {
            // Add Binding
            addBinding(targetModel.findByPath(trace.previousPath!!, javaClass<MBinding>())!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        }
    }

    fun manageModelRemoveTrace(trace: ModelRemoveTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        //                    System.out.println(trace);
        val element = currentModel.findByPath(trace.objPath)

        if (element != null && (element is Group || element is Channel || element is ComponentInstance || element is ContainerNode)) {
            removeInstance(element as Instance, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        }  else if (trace.refName.equals("bindings")) {
            removeBinding(element as MBinding, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        }
    }

    fun manageModelSetTrace(trace: ModelSetTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        //                    System.out.println(trace);
        if (trace.refName.equals("started")) {
            if (trace.content.equals("true")) {
                startInstance(targetModel.findByPath(trace.srcPath, javaClass<Instance>())!!, currentModel, targetModel, adaptationModel)
            } else {
                var instance: Instance? = null
                if (currentModel.findByPath(trace.srcPath) != null) {
                    instance = currentModel.findByPath(trace.srcPath, javaClass<Instance>())
                } else {
                    instance = targetModel.findByPath(trace.srcPath, javaClass<Instance>())
                }
                stopInstance(instance!!, currentModel, targetModel, adaptationModel)
            }
        } else if (trace.refName.equals("value")) {
            updateDictionary(targetModel.findByPath(trace.srcPath)!!.eContainer()!!.eContainer() as Instance, currentModel, targetModel, adaptationModel)

        }
    }

    fun dropChannelAndTypeDefinitionAndDeployUnit(currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        var foundDeployUnitsToRemove = HashSet<String>()
        var foundTypeDefinitionsToRemove = HashSet<String>()
        var foundChannelsToRemove = HashSet<String>()
        currentNode.visit(object : ModelVisitor(){
            override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                if(elem is DeployUnit){
                    foundDeployUnitsToRemove.add(elem.path()!!)
                } else if (elem is TypeDefinition) {
                    foundTypeDefinitionsToRemove.add(elem.path()!!)
                } else if (elem is Channel) {
                    foundChannelsToRemove.add(elem.path()!!)
                }
            }

        }, true, true, true)
        targetNode.visit(object : ModelVisitor(){
            override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                if(elem is DeployUnit){
                    foundDeployUnitsToRemove.remove(elem.path()!!)
                } else if (elem is TypeDefinition) {
                    foundTypeDefinitionsToRemove.remove(elem.path()!!)
                } else if (elem is Channel) {
                    foundChannelsToRemove.remove(elem.path()!!)
                }
            }

        }, true, true, true)
        // Remove channels that are not used anymore in the target model
        foundChannelsToRemove.forEach {
            channelPath ->
            val channel = currentModel.findByPath(channelPath, javaClass<Channel>())
            if (channel != null) {
                removeInstance(channel, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        }
        // Remove TypeDefinitions that are not used anymore in the target model
        foundTypeDefinitionsToRemove.forEach {
            typeDefinitionPath ->
            val typeDefinition = currentModel.findByPath(typeDefinitionPath, javaClass<TypeDefinition>())
            if (typeDefinition != null) {
                removeTypeDefinition(typeDefinition, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        }
        // Remove DeployUnits that are not used anymore in the target model
        foundDeployUnitsToRemove.forEach {
            deployUnitPath ->
            val deployUnit = currentModel.findByPath(deployUnitPath, javaClass<DeployUnit>())
            if (deployUnit != null) {
                removeDeployUnit(deployUnit, currentModel, targetModel, adaptationModel)
            }
        }

    }

    fun updateDictionary(instance: Instance, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (updatedInstances.filter { inst -> inst.name.equals(instance.name) }.isEmpty()) {
            // Update dictionary
            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
            ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance)
            ccmd.ref = instance
            adaptationModel.addAdaptations(ccmd)
        }
    }

    fun startInstance(instance: Instance, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Start instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)
    }

    fun stopInstance(instance: Instance, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Stop instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)
    }

    fun addBinding(element: MBinding, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add binding TODO must to check and maybe to complete according to FragmentBinding
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding)
        ccmd.ref = element
        adaptationModel.addAdaptations(ccmd)

        if (registry!!.get(element.hub!!.path()) == null) {
            addInstance(element.hub!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        }
    }

    fun removeBinding(element: MBinding, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove binding TODO must to check and maybe to complete according to FragmentBinding
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding)
        ccmd.ref = element
        adaptationModel.addAdaptations(ccmd)

        // channel connected to this binding will be removed if it doesn't appear anymore in the channels connected to one of the components available on the currentNode (see dropChannelAndTypeDefinitionAndDeployUnit)
    }


    fun addInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = (currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance))
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)

        if (registry!!.get(targetModel.findTypeDefinitionsByID(instance.typeDefinition!!.name)) == null) {
            // Add TypeDefinition
            addTypeDefinition(instance.typeDefinition!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        }
    }

    fun removeInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = (currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance))
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)

        // Stop instance
        stopInstance(instance, currentModel, targetModel, adaptationModel)

        // typeDefinition connected to this instance will be removed if it doesn't appear anymore in the typeDefinitions used by one of the components available on the currentNode (see dropChannelAndTypeDefinitionAndDeployUnit)
    }

    fun addTypeDefinition(typeDefinition: TypeDefinition, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add TypeDefinition
        val ccmd1 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd1.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType)
        ccmd1.ref = targetModel.findTypeDefinitionsByID(typeDefinition.name)
        adaptationModel.addAdaptations(ccmd1)

        // Add DeployUnit(s)
        val deployUnits = typeDefinition.deployUnits.filter {
            deployUnit ->
            deployUnit.targetNodeType!!.name == currentNode.typeDefinition!!.name
        }
        deployUnits.forEach {
            deployUnit ->
            if (registry!!.get(deployUnit.path()) == null) {
                addDeployUnit(deployUnit, currentModel, targetModel, adaptationModel)
            }
        }
    }

    fun removeTypeDefinition(typeDefinition: TypeDefinition, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // remove TypeDefinition
        val ccmd1 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd1.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType)
        ccmd1.ref = targetModel.findTypeDefinitionsByID(typeDefinition.name)
        adaptationModel.addAdaptations(ccmd1)

        // deployUnits connected to this typeDefinition will be removed if they don't appear anymore in the deployUnits used by one of the typeDefinition available on the currentNode (see dropChannelAndTypeDefinitionAndDeployUnit)
    }

    fun addDeployUnit(deployUnit: DeployUnit, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd1 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd1.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddDeployUnit)
        ccmd1.ref = deployUnit
        adaptationModel.addAdaptations(ccmd1)
        deployUnit.requiredLibs.forEach {
            requiredLib ->
            if (registry!!.get(requiredLib) == null) {
                addDeployUnit(requiredLib, currentModel, targetModel, adaptationModel)
            }
        }
    }

    fun removeDeployUnit(deployUnit: DeployUnit, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd1 = adaptationModelFactory.createAdaptationPrimitive()
        ccmd1.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveDeployUnit)
        ccmd1.ref = deployUnit
        adaptationModel.addAdaptations(ccmd1)
        
        // deployUnits connected to this typeDefinition will be removed if they don't appear anymore in the deployUnits used by one of the typeDefinition available on the currentNode (see dropChannelAndTypeDefinitionAndDeployUnit)
    }
}
