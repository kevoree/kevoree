package org.kevoree.kompare

import org.kevoree.*
import org.kevoreeadaptation.*
import org.kevoree.kompare.sub.Kompare2
import org.kevoree.log.Log

open class KevoreeKompareBean : Kompare2, KevoreeScheduler {
    override var previousStep: ParallelStep? = null
    override var currentSteps: ParallelStep? = null

    override var adaptationModelFactory: KevoreeAdaptationFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()

    fun kompare(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {

        var adaptationModel = compareModels(actualModel, targetModel, nodeName)

        //logger.debug("after Hara Kiri detect")
        val afterPlan = plan(adaptationModel, nodeName)
        return afterPlan
    }

    open fun compareModels(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {

        val adaptationModelFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()
        var adaptationModel = adaptationModelFactory.createAdaptationModel()
        //STEP 0 - FOUND LOCAL NODE
        var actualLocalNode = actualModel.findByPath("nodes[" + nodeName + "]", javaClass<ContainerNode>())
        var updateLocalNode = targetModel.findByPath("nodes[" + nodeName + "]", javaClass<ContainerNode>())
        if(actualLocalNode == null && updateLocalNode == null){
            Log.warn("Empty Kompare because {} not found in current nor in target model ", nodeName)
            return adaptationModel
        }
        adaptationModel = getUpdateNodeAdaptationModel(actualLocalNode, updateLocalNode, actualModel, targetModel, nodeName)
        return transformPrimitives(adaptationModel, actualModel)
    }

    private fun transformPrimitives(adaptationModel: AdaptationModel, actualModel: ContainerRoot): AdaptationModel {
        //TRANSFORME UPDATE
        for(adaptation in adaptationModel.adaptations){
            when(adaptation.primitiveType!!.name) {
                JavaSePrimitive.UpdateType -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType)
                    rcmd.ref = adaptation.ref!!
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)
                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType)
                    acmd.ref = adaptation.ref!!
                    adaptationModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding)
                    rcmd.ref = adaptation.ref!!
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding)
                    acmd.ref = adaptation.ref!!
                    adaptationModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateFragmentBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding)
                    rcmd.ref = adaptation.ref!!
                    rcmd.targetNodeName = adaptation.targetNodeName
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding)
                    acmd.ref = adaptation.ref!!
                    acmd.targetNodeName = adaptation.targetNodeName
                    adaptationModel.addAdaptations(acmd)
                }

                JavaSePrimitive.UpdateInstance -> {
                    val stopcmd = adaptationModelFactory.createAdaptationPrimitive()
                    stopcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance)
                    stopcmd.ref = (adaptation.ref as Array<Any>).get(0)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(stopcmd)

                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance)
                    rcmd.ref = (adaptation.ref as Array<Any>).get(0)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance)
                    acmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(acmd)

                    val uDiccmd = adaptationModelFactory.createAdaptationPrimitive()
                    uDiccmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance)
                    uDiccmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(uDiccmd)

                    val startcmd = adaptationModelFactory.createAdaptationPrimitive()
                    startcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance)
                    startcmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(startcmd)
                }
                else -> {
                }
            }
        }
        return adaptationModel;
    }

}
