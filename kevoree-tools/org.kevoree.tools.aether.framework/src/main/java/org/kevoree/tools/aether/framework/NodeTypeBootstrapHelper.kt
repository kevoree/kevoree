package org.kevoree.tools.aether.framework

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.api.Bootstraper
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler
import org.kevoree.framework.*
import java.io.File
import org.kevoree.*
import org.kevoree.kcl.KevoreeJarClassLoader
import java.util
import org.kevoree.impl.DefaultKevoreeFactory
import org.slf4j.LoggerFactory
import java.util.ArrayList
import org.kevoree.framework.kaspects.TypeDefinitionAspect

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

open class NodeTypeBootstrapHelper: Bootstraper, KCLBootstrap {

    override fun resolveKevoreeArtifact(artId: String, groupId: String, version: String): File {
        val l = ArrayList<String>()
        l.add("http://maven.kevoree.org/release")
        l.add("http://maven.kevoree.org/snapshots")
        return resolveArtifact(artId, groupId, version, l)
    }

    val kevoreeFactory: KevoreeFactory = DefaultKevoreeFactory()
    var _kevoreeLogService: org.kevoree.api.service.core.logging.KevoreeLogService? = null
    val logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun getKevoreeLogService(): org.kevoree.api.service.core.logging.KevoreeLogService {
        return _kevoreeLogService!!
    }
    override fun setKevoreeLogService(ls: org.kevoree.api.service.core.logging.KevoreeLogService) {
        _kevoreeLogService = ls
    }

    var classLoaderHandler = JCLContextHandler()

    override fun bootstrapNodeType(model: ContainerRoot, destNodeName: String, mservice: KevoreeModelHandlerService, kevsEngineFactory: KevScriptEngineFactory): org.kevoree.api.NodeType? {
        //LOCATE NODE
        val nodeOption = model.findNodesByID(destNodeName)
        if(nodeOption != null){
            val nodeTypeDeployUnitList = nodeOption.getTypeDefinition()!!.getDeployUnits()
            if (nodeTypeDeployUnitList.size > 0) {
                val classLoader = installNodeType(nodeOption.getTypeDefinition() as org.kevoree.NodeType)
                if (classLoader != null) {
                    val clazz = classLoader.loadClass(nodeOption.getTypeDefinition()!!.getBean())
                    val nodeType = clazz!!.newInstance() as AbstractNodeType
                    //ADD INSTANCE DICTIONARY
                    val dictionary = java.util.HashMap<String, Any>()
                    val dictionaryType = nodeOption.getTypeDefinition()!!.getDictionaryType()
                    if (dictionaryType != null) {
                        for(dv in dictionaryType.getDefaultValues()) {
                            dictionary.put(dv.getAttribute()!!.getName(), dv.getValue())
                        }
                    }

                    val dictionaryModel = nodeOption.getDictionary()

                    if (dictionaryModel != null) {
                        for (v in dictionaryModel.getValues()) {
                            dictionary.put(v.getAttribute()!!.getName(), v.getValue())
                        }
                    }
                    nodeType.setDictionary(dictionary)
                    nodeType.setNodeName(destNodeName)
                    //INJECT SERVICE HANDLER
                    nodeType.setModelService(mservice)
                    nodeType.setKevScriptEngineFactory(kevsEngineFactory)
                    nodeType.setBootStrapperService(this)
                    return nodeType
                } else {
                    return null
                }
            } else {
                logger.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?")
                return null
            }
        } else {
            logger.error("Node not found using name " + destNodeName);
            return null
        }
    }


    /* Bootstrap node type bundle in local environment */
    private fun installNodeType(nodeType: org.kevoree.NodeType): ClassLoader? {
        val superTypeBootStrap = nodeType.getSuperTypes().all{ superType -> installNodeType(superType as org.kevoree.NodeType) != null }
        if (superTypeBootStrap) {
            var kcl: ClassLoader? = null
            nodeType.getDeployUnits().all{ ct ->
                val dpRes = ct.getRequiredLibs().all{ tp ->
                    val idp = installDeployUnit(tp)
                    idp != null
                }
                val kcl_opt = installDeployUnit(ct)
                if(kcl_opt != null){
                    kcl = kcl_opt
                }
                (kcl_opt != null) && dpRes
            }
            return kcl //TODO
        } else {
            logger.error("Super type of " + nodeType.getName() + " was not completely installed")
            return null
        }
    }

    override fun getKevoreeClassLoaderHandler(): KevoreeClassLoaderHandler {
        return classLoaderHandler
    }


    override fun resolveDeployUnit(du: DeployUnit): File = AetherUtil.resolveDeployUnit(du)!!

    override fun close() {
        classLoaderHandler.clear()
        classLoaderHandler.stop()
    }

    override fun clear() {
        classLoaderHandler.clear()
    }

    fun registerManuallyDeployUnit(artefactID: String, groupID: String, version: String, kcl: KevoreeJarClassLoader) {
        val du = kevoreeFactory.createDeployUnit()
        du.setUnitName(artefactID)
        du.setGroupName(groupID)
        du.setVersion(version)
        classLoaderHandler.manuallyAddToCache(du, kcl)
    }

    override fun resolveArtifact(artId: String, groupId: String, version: String, extension: String, repos: List<String>): File {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, extension, repos)!!
    }
    override fun resolveArtifact(artId: String, groupId: String, version: String, repos: List<String>): File {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, repos)!!
    }

    fun bootstrapGroupType(model: ContainerRoot, destGroupName: String, mservice: KevoreeModelHandlerService): AbstractGroupType? {
        //LOCATE NODE
        val optgroup = model.findGroupsByID(destGroupName)
        if(optgroup != null) {
            val groupTypeDeployUnitList = optgroup.getTypeDefinition()!!.getDeployUnits()
            if (groupTypeDeployUnitList.size > 0) {
                val kcl = installGroupTyp(optgroup.getTypeDefinition() as GroupType)
                if (kcl != null) {
                    val activatorPackage = KevoreeGeneratorHelper().getTypeDefinitionGeneratedPackage(optgroup.getTypeDefinition(), "JavaSENode")
                    val activatorName = optgroup.getTypeDefinition()!!.getName() + "Activator"
                    val clazz = kcl.loadClass(activatorPackage + "." + activatorName)

                    val groupActivator = clazz!!.newInstance() as org.kevoree.framework.osgi.KevoreeGroupActivator
                    val groupType = groupActivator.callFactory()!! as AbstractGroupType

                    //ADD INSTANCE DICTIONARY
                    val dictionary: java.util.HashMap<String, Any> = java.util.HashMap<String, Any>()

                    val dictionaryType = optgroup.getTypeDefinition()!!.getDictionaryType()

                    if (dictionaryType != null) {
                        for(dv in dictionaryType.getDefaultValues()) {
                            dictionary.put(dv.getAttribute()!!.getName(), dv.getValue())
                        }
                    }
                    val dictionaryModel = optgroup.getDictionary()
                    if (dictionaryModel != null) {
                        for(v in dictionaryModel.getValues()) {
                            dictionary.put(v.getAttribute()!!.getName(), v.getValue())
                        }
                    }
                    groupType.getDictionary()!!.putAll(dictionary)
                    groupType.setName(destGroupName)
                    groupType.setModelService(mservice)
                    groupType.setBootStrapperService(this)
                    return groupType
                } else {
                    return null
                }
            } else {
                logger.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?")
                return null
            }
        }else {
            logger.error("Group not found using name " + destGroupName); return null
        }
    }

    private fun installGroupTyp(groupType: GroupType): ClassLoader? {
        val superTypeBootStrap = groupType.getSuperTypes().all{ superType -> installGroupTyp(superType as GroupType) != null }
        if (superTypeBootStrap) {
            //FAKE NODE TODO
            val fakeNode = kevoreeFactory.createContainerNode()
            val javaseTD = (groupType.eContainer() as ContainerRoot).findTypeDefinitionsByID("JavaSENode")
            if(javaseTD != null){
                fakeNode.setTypeDefinition(javaseTD)
            }
            var ct: DeployUnit? = null
            try {
                ct = TypeDefinitionAspect().foundRelevantDeployUnit(groupType, fakeNode)
            } catch(e: Exception) {
            }
            if (ct != null) {
                var kcl: ClassLoader? = null
                val dpRes = ct!!.getRequiredLibs().all {
                    tp ->
                    installDeployUnit(tp) != null
                }
                val kcl_opt = installDeployUnit(ct!!)
                if(kcl_opt != null){
                    kcl = kcl_opt
                }

                kcl_opt != null && dpRes
                return kcl //TODO
            } else {
                return null
            }
        } else {
            return null
        }
    }


}
