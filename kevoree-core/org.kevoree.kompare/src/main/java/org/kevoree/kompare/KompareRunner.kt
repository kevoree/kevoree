package org.kevoree.kompare

import org.kevoree.DeployUnit
import org.kevoree.ComponentInstance
import org.kevoreeadaptation.AdaptationModel
import org.kevoreeadaptation.ParallelStep
import org.kevoreeadaptation.AdaptationPrimitive
import org.kevoree.MBinding
import org.kevoree.NamedElement
import java.util.HashMap
import org.kevoree.ContainerRoot
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.modeling.api.json.JSONModelSerializer

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/10/13
 * Time: 18:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */
fun main(args: Array<String>) {
    val factory = DefaultKevoreeFactory()

    val currentModel = factory.createContainerRoot()

    val currentDeployUnit = factory.createDeployUnit()
    currentDeployUnit.unitName = "deployUnit1"
    currentDeployUnit.groupName = "org.kevoree"
    currentDeployUnit.version = "1.0"
    currentDeployUnit.hashcode = "" + System.nanoTime();

    val currentDictionaryAttribute1 = factory.createDictionaryAttribute()
    currentDictionaryAttribute1.fragmentDependant = false
    currentDictionaryAttribute1.name = "dictionaryAttribute1"

    val currentDictionaryType1 = factory.createDictionaryType()
    currentDictionaryType1.addAttributes(currentDictionaryAttribute1)

    val currentTypeDefinition1 = factory.createComponentType()
    currentTypeDefinition1.name = "TypeDefinition1"
    currentTypeDefinition1.abstract = false
    currentTypeDefinition1.addDeployUnits(currentDeployUnit)
    currentTypeDefinition1.dictionaryType = currentDictionaryType1


    val currentDictionaryAttribute2 = factory.createDictionaryAttribute()
    currentDictionaryAttribute2.fragmentDependant = false
    currentDictionaryAttribute2.name = "dictionaryAttribute2"

    val currentDictionaryType2 = factory.createDictionaryType()
    currentDictionaryType2.addAttributes(currentDictionaryAttribute2)

    val currentTypeDefinition2 = factory.createNodeType()
    currentTypeDefinition2.name = "TypeDefinition2"
    currentTypeDefinition2.abstract = false
    currentTypeDefinition2.addDeployUnits(currentDeployUnit)
    currentTypeDefinition2.dictionaryType = currentDictionaryType2

    val currentComponent = factory.createComponentInstance()
    currentComponent.name = "component1"
    currentComponent.started = true
    currentComponent.typeDefinition = currentTypeDefinition1
    currentComponent.dictionary = factory.createDictionary()

    val currentDictionaryValue = factory.createDictionaryValue()
    currentDictionaryValue.attribute = currentDictionaryAttribute1
    currentDictionaryValue.value = "toto1"
    currentComponent.dictionary!!.addValues(currentDictionaryValue)

    val currentNode = factory.createContainerNode()
    currentNode.name = "node"
    currentNode.typeDefinition = currentTypeDefinition2
    currentNode.started = true
    currentDeployUnit.targetNodeType = currentTypeDefinition2


    var adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddInstance
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveInstance
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddType
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveType
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddDeployUnit
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveDeployUnit
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.StartInstance
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.StopInstance
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.UpdateDictionaryInstance
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)


    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddBinding
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveBinding
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddThirdParty
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveThirdParty
    currentModel.addAdaptationPrimitiveTypes(adaptationPrimitive)


    currentModel.addDeployUnits(currentDeployUnit)
    currentModel.addTypeDefinitions(currentTypeDefinition1)
    currentModel.addTypeDefinitions(currentTypeDefinition2)
    currentModel.addNodes(currentNode)
    currentNode.addComponents(currentComponent)

    val targetModel = factory.createContainerRoot()
    targetModel.generated_KMF_ID = currentModel.generated_KMF_ID

    val targetDeployUnit = factory.createDeployUnit()
    targetDeployUnit.generated_KMF_ID = currentDeployUnit.generated_KMF_ID
    targetDeployUnit.unitName = "deployUnit1"
    targetDeployUnit.groupName = "org.kevoree"
    targetDeployUnit.version = "1.0"
    targetDeployUnit.hashcode = currentDeployUnit.hashcode + "1";

    val targetDictionaryAttribute1 = factory.createDictionaryAttribute()
    targetDictionaryAttribute1.fragmentDependant = true
    targetDictionaryAttribute1.name = "dictionaryAttribute1"
    targetDictionaryAttribute1.optional = true

    val targetDictionaryType1 = factory.createDictionaryType()
    targetDictionaryType1.generated_KMF_ID = currentDictionaryType1.generated_KMF_ID
    targetDictionaryType1.addAttributes(targetDictionaryAttribute1)

    val targetTypeDefinition1 = factory.createComponentType()
    targetTypeDefinition1.name = "TypeDefinition1"
    targetTypeDefinition1.abstract = false
    targetTypeDefinition1.addDeployUnits(targetDeployUnit)
    targetTypeDefinition1.dictionaryType = targetDictionaryType1


    val targetDictionaryAttribute2 = factory.createDictionaryAttribute()
    targetDictionaryAttribute2.fragmentDependant = false
    targetDictionaryAttribute2.name = "dictionaryAttribute2"

    val targetDictionaryType2 = factory.createDictionaryType()
    targetDictionaryType2.generated_KMF_ID = currentDictionaryType2.generated_KMF_ID
    targetDictionaryType2.addAttributes(targetDictionaryAttribute2)

    val targetTypeDefinition2 = factory.createNodeType()
    targetTypeDefinition2.name = "TypeDefinition2"
    targetTypeDefinition2.abstract = false
    targetTypeDefinition2.addDeployUnits(targetDeployUnit)
    targetTypeDefinition2.dictionaryType = targetDictionaryType2

    val targetComponent = factory.createComponentInstance()
    targetComponent.name = "component1"
    targetComponent.started = true
    targetComponent.typeDefinition = targetTypeDefinition1
    targetComponent.dictionary = factory.createDictionary()
    targetComponent.dictionary!!.generated_KMF_ID = currentComponent.dictionary!!.generated_KMF_ID


    val targetDictionaryValue = factory.createDictionaryValue()
    targetDictionaryValue.generated_KMF_ID = currentDictionaryValue.generated_KMF_ID
    targetDictionaryValue.attribute = targetDictionaryAttribute1
    targetDictionaryValue.value = "toto1"
    targetComponent.dictionary!!.addValues(targetDictionaryValue)

    val targetNode = factory.createContainerNode()
    targetNode.name = "node"
    targetNode.typeDefinition = targetTypeDefinition2
    targetNode.started = true
    targetDeployUnit.targetNodeType = targetTypeDefinition2

    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddInstance
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveInstance
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddType
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveType
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddDeployUnit
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveDeployUnit
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.StartInstance
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.StopInstance
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.UpdateDictionaryInstance
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)


    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddBinding
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveBinding
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.AddThirdParty
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)
    adaptationPrimitive = factory.createAdaptationPrimitiveType()
    adaptationPrimitive.name = JavaSePrimitive.RemoveThirdParty
    targetModel.addAdaptationPrimitiveTypes(adaptationPrimitive)


    targetModel.addDeployUnits(targetDeployUnit)
    targetModel.addTypeDefinitions(targetTypeDefinition1)
    targetModel.addTypeDefinitions(targetTypeDefinition2)
    targetModel.addNodes(targetNode)
    targetNode.addComponents(targetComponent)

    val seralizer = JSONModelSerializer()
    println(seralizer.serialize(currentModel))
    println("\n\n")
    println(seralizer.serialize(targetModel))

//    val currentModel = KevoreeXmiHelper.load("/home/edaubert/firstmodel.kev")!!
//    val targetModel = KevoreeXmiHelper.load("/home/edaubert/firstmodel-without1binding.kev")!!

    printAdaptations(Kompare3(populateRegistry(currentModel, "node")).compareModels(currentModel, targetModel, "node"))
//    printAdaptations(Kompare3().compareModels(targetModel, currentModel, "node0"))

//        val currentModel = KevoreeXmiHelper.load("/home/edaubert/secondmodel.kev")!!
//        val targetModel = KevoreeXmiHelper.load("/home/edaubert/secondmodel-withoutbindings.kev")!!

//        printAdaptations(Kompare3().compareModels(currentModel, targetModel, "node0"))
//        printAdaptations(Kompare3().compareModels(targetModel, currentModel, "node0"))

}

fun populateRegistry(model : ContainerRoot, nodeName : String) : HashMap<String, Any> {
    val currentNode = model.findNodesByID(nodeName)
    val registry = HashMap<String, Any>()
    currentNode!!.visit(object : ModelVisitor(){
        override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
            registry.put(elem.path()!!, elem.path()!!)
        }

    }, true, true, true)

    return registry
}


fun printStep(kompareModel: AdaptationModel) {
    printStep(kompareModel.orderedPrimitiveSet, 0)
}

fun printStep(step: ParallelStep?, index: Int) {
    if (step != null && step.adaptations.size() > 0) {
        System.out.println("Step n° " + index)
        for (adaptation in step.adaptations) {
            printAdaptation(adaptation);
        }
        printStep(step.nextStep, index + 1)
    }
}

fun printAdaptations(kompareModel: AdaptationModel) {
    for (adaptation in kompareModel.adaptations) {
        printAdaptation(adaptation)
    }
}

fun printAdaptation (adaptation: AdaptationPrimitive) {
    System.out.print(adaptation.primitiveType!!.name + ": ")
    if (adaptation.primitiveType!!.name.equals(JavaSePrimitive.AddBinding) || adaptation.primitiveType!!.name.equals(JavaSePrimitive.RemoveBinding)) {
        val binding = adaptation.ref!! as MBinding
        System.out.print(binding.hub!!.name + "<->" + (binding.port!!.eContainer() as ComponentInstance).name + "." + binding.port!!.portTypeRef!!.name)
    } else if (adaptation.primitiveType!!.name.equals(JavaSePrimitive.AddInstance) || adaptation.primitiveType!!.name.equals(JavaSePrimitive.RemoveInstance)
    || adaptation.primitiveType!!.name.equals(JavaSePrimitive.StartInstance) || adaptation.primitiveType!!.name.equals(JavaSePrimitive.StopInstance)
    || adaptation.primitiveType!!.name.equals(JavaSePrimitive.AddType) || adaptation.primitiveType!!.name.equals(JavaSePrimitive.RemoveType)) {
        System.out.print((adaptation.ref!! as NamedElement).name)
    } else if (adaptation.primitiveType!!.name.equals(JavaSePrimitive.AddDeployUnit) || adaptation.primitiveType!!.name.equals(JavaSePrimitive.RemoveDeployUnit)) {
        System.out.print((adaptation.ref!! as DeployUnit).groupName + ":" + (adaptation.ref!! as DeployUnit).unitName + ":" + (adaptation.ref!! as DeployUnit).version)
    } else if (adaptation.primitiveType!!.name.equals(JavaSePrimitive.UpdateDictionaryInstance)) {
        System.out.print((adaptation.ref!! as NamedElement).name)
    }
    System.out.println()
}