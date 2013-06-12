package org.kevoree.kompare

import org.kevoree.*
import org.kevoreeadaptation.*
import org.kevoree.kompare.sub.Kompare2
import org.kevoree.log.Log

open class KevoreeKompareBean: Kompare2, KevoreeScheduler {
    override var step: ParallelStep? = null
    override var currentStep: ParallelStep? = null

    override var adaptationModelFactory: KevoreeAdaptationFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()

    fun kompare(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {

        var adaptationModel = compareModels(actualModel, targetModel, nodeName)

        //logger.debug("after Hara Kiri detect")
        val afterPlan = plan(adaptationModel, nodeName)
        return afterPlan
    }

    open fun compareModels(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String) : AdaptationModel {

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

    private fun transformPrimitives(adaptationModel : AdaptationModel, actualModel: ContainerRoot): AdaptationModel {
        //TRANSFORME UPDATE
        for(adaptation in adaptationModel.getAdaptations()){
            when(adaptation.getPrimitiveType()!!.getName()) {
                JavaSePrimitive.UpdateType -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType))
                    rcmd.setRef(adaptation.getRef()!!)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)
                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType))
                    acmd.setRef(adaptation.getRef()!!)
                    adaptationModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding))
                    rcmd.setRef(adaptation.getRef()!!)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding))
                    acmd.setRef(adaptation.getRef()!!)
                    adaptationModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateFragmentBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding))
                    rcmd.setRef(adaptation.getRef()!!)
                    rcmd.setTargetNodeName(adaptation.getTargetNodeName())
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding))
                    acmd.setRef(adaptation.getRef()!!)
                    acmd.setTargetNodeName(adaptation.getTargetNodeName())
                    adaptationModel.addAdaptations(acmd)
                }

                JavaSePrimitive.UpdateInstance -> {
                    val stopcmd = adaptationModelFactory.createAdaptationPrimitive()
                    stopcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance))
                    stopcmd.setRef((adaptation.getRef() as Array<Any>).get(0))
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(stopcmd)

                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance))
                    rcmd.setRef((adaptation.getRef() as Array<Any>).get(0))
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance))
                    acmd.setRef((adaptation.getRef() as Array<Any>).get(1))
                    adaptationModel.addAdaptations(acmd)

                    val uDiccmd = adaptationModelFactory.createAdaptationPrimitive()
                    uDiccmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
                    uDiccmd.setRef((adaptation.getRef() as Array<Any>).get(1))
                    adaptationModel.addAdaptations(uDiccmd)

                    val startcmd = adaptationModelFactory.createAdaptationPrimitive()
                    startcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance))
                    startcmd.setRef((adaptation.getRef() as Array<Any>).get(1))
                    adaptationModel.addAdaptations(startcmd)
                }
                else -> {
                }
            }
        }
        return adaptationModel;
    }

}
