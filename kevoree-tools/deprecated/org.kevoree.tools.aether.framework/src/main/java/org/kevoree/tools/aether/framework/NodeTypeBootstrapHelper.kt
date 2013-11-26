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
import java.util.ArrayList
import org.kevoree.framework.kaspects.TypeDefinitionAspect
import org.kevoree.log.Log
import java.util.List
import org.kevoree.kcl.Klassloader

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

open class NodeTypeBootstrapHelper : Bootstraper, KCLBootstrap {

    override fun resolveKevoreeArtifact(artId: String, groupId: String, version: String): File? {
        val l = ArrayList<String>()
        if(version.contains("SNAPSHOT") || version.contains("LATEST")){
            l.add("http://oss.sonatype.org/content/groups/public/")
        } else {
            l.add("http://repo1.maven.org/maven2")
        }
        return resolveArtifact(artId, groupId, version, l)
    }

    val kevoreeFactory: KevoreeFactory = DefaultKevoreeFactory()
    var _kevoreeLogService: org.kevoree.api.service.core.logging.KevoreeLogService? = null

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
            val nodeTypeDeployUnit = nodeOption.typeDefinition!!.deployUnit
            if (nodeTypeDeployUnit!=null) {
                val classLoader = installNodeType(nodeOption.typeDefinition as org.kevoree.NodeType)
                if (classLoader != null) {
                    val clazz = classLoader.loadClass(nodeOption!!.typeDefinition!!.bean)
                    val nodeType = clazz!!.newInstance() as AbstractNodeType
                    //ADD INSTANCE DICTIONARY
                    val dictionary = java.util.HashMap<String, Any>()
                    val dictionaryType = nodeOption!!.typeDefinition!!.dictionaryType
                    if (dictionaryType != null) {
                        for(dv in dictionaryType.defaultValues) {
                            dictionary.put(dv!!.attribute!!.name!!, dv.value!!)
                        }
                    }

                    val dictionaryModel = nodeOption.dictionary

                    if (dictionaryModel != null) {
                        for (v in dictionaryModel.values) {
                            dictionary.put(v!!.attribute!!.name!!, v.value!!)
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
                Log.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?")
                return null
            }
        } else {
            Log.error("Node not found using name " + destNodeName);
            return null
        }
    }


    /* Bootstrap node type bundle in local environment */
    private fun installNodeType(nodeType: org.kevoree.NodeType): Klassloader? {
        val superKCLs = ArrayList<Klassloader>()
        val superTypeBootStrap = nodeType.superTypes.all {
            superType ->
            val superKCL = installNodeType(superType as org.kevoree.NodeType)
            if (superKCL != null) {
                superKCLs.add(superKCL)
            }
            superKCL != null
        }
        if (superTypeBootStrap) {
            var kcl: Klassloader? = null
                val kcl_opt = recursivelyInstallDeployUnit(nodeType.deployUnit!!)
                if(kcl_opt != null){
                    kcl = kcl_opt
                }
                kcl_opt != null
            if (kcl == null) {
                Log.error("Unable to install the Node Type, maybe some libs cannot be installed")
            } else {
                superKCLs.forEach {
                    superKCL ->
                    (kcl as KevoreeJarClassLoader).addSubClassLoader(superKCL)
                }
            }
            return kcl //TODO
        } else {
            Log.error("Super type of " + nodeType.name + " was not completely installed")
            return null
        }
    }


    override fun getKevoreeClassLoaderHandler(): KevoreeClassLoaderHandler {
        return classLoaderHandler
    }


    override fun resolveDeployUnit(du: DeployUnit): File? = AetherUtil.resolveDeployUnit(du)

    override fun close() {
        classLoaderHandler.clear()
        classLoaderHandler.stop()
    }

    override fun clear() {
        classLoaderHandler.clear()
    }

    fun registerManuallyDeployUnit(artefactID: String, groupID: String, version: String, kcl: KevoreeJarClassLoader) {
        val du = kevoreeFactory.createDeployUnit()
        du.name = artefactID
        du.groupName = groupID
        du.version = version
        classLoaderHandler.manuallyAddToCache(du, kcl)
    }

    override fun resolveArtifact(artId: String, groupId: String, version: String, extension: String, repos: jet.List<String>): File? {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, extension, repos)
    }
    override fun resolveArtifact(artId: String, groupId: String, version: String, repos: jet.List<String>): File? {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, repos)
    }

    fun bootstrapGroupType(model: ContainerRoot, destGroupName: String, mservice: KevoreeModelHandlerService): AbstractGroupType? {
        //LOCATE NODE
        val optgroup = model.findGroupsByID(destGroupName)
        if(optgroup != null) {
            val groupTypeDeployUnit = optgroup.typeDefinition!!.deployUnit
            if (groupTypeDeployUnit!=null) {
                val kcl = installGroupType(optgroup.typeDefinition as GroupType)
                if (kcl != null) {
                    val clazz = kcl.loadClass(optgroup!!.typeDefinition!!.bean)
                    val groupType = clazz!!.newInstance() as AbstractGroupType

                    //ADD INSTANCE DICTIONARY
                    val dictionary: java.util.HashMap<String, Any> = java.util.HashMap<String, Any>()

                    val dictionaryType = optgroup!!.typeDefinition!!.dictionaryType

                    if (dictionaryType != null) {
                        for(dv in dictionaryType.defaultValues) {
                            dictionary.put(dv.attribute!!.name!!, dv.value!!)
                        }
                    }
                    val dictionaryModel = optgroup.dictionary
                    if (dictionaryModel != null) {
                        for(v in dictionaryModel.values) {
                            dictionary.put(v.attribute!!.name!!, v.value!!)
                        }
                    }
                    groupType.getDictionary()!!.putAll(dictionary)
                    groupType.setName(destGroupName)
                    groupType.setModelService(mservice)
                    groupType.setBootStrapperService(this)
                    return groupType
                } else {
                    Log.error("Unable to bootstrap Group so KCL pointer is null")
                    return null
                }
            } else {
                Log.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?")
                return null
            }
        }else {
            Log.error("Group not found using name " + destGroupName)
            return null
        }
    }

    private fun installGroupType(groupType: GroupType): Klassloader? {
        if (groupType.deployUnit != null ) {
            val superKCLs = ArrayList<Klassloader>()
            val superTypeBootStrap = groupType.superTypes.all {
                superType ->
                val superKCL = installGroupType(superType as org.kevoree.GroupType)
                if (superKCL != null) {
                    superKCLs.add(superKCL)
                }
                superKCL != null
            }
            //            val superTypeBootStrap = groupType.superTypes.all { superType -> installGroupTyp(superType as GroupType) != null }
            if (superTypeBootStrap) {
                var ct: DeployUnit? = null
                try {
                    ct = TypeDefinitionAspect().foundRelevantDeployUnit(groupType)
                } catch(e: Exception) {
                    e.printStackTrace()
                }
                if (ct != null) {
                    var kcl: Klassloader? = recursivelyInstallDeployUnit(ct!!)
                    if (kcl == null) {
                        Log.error("Unable to install the Node Type, maybe some libs cannot be installed")
                    } else {
                        superKCLs.forEach {
                            superKCL ->
                            (kcl as KevoreeJarClassLoader).addSubClassLoader(superKCL)
                        }
                    }
                    return kcl
                } else {
                    Log.error("Relevant Deploy Unit not found for " + groupType.name)
                    return null
                }
            } else {
                Log.error("Super installation failed")
                return null
            }
        } else {
            Log.error("The GroupType is not able to be executed on JavaSENode so the group cannot be instanciated. Please add a DeployUnit which enable the groupType execution on JavaSeNode.")
            return null
        }
    }

    private fun recursivelyInstallDeployUnit(du: DeployUnit): Klassloader? {
        val kcl_opt = installDeployUnit(du)

        var dpRes = true
        if (kcl_opt != null) {
            dpRes = du.requiredLibs.all { tp ->
                val idp = recursivelyInstallDeployUnit(tp)
                if (idp != null) {
                    kcl_opt.addSubClassLoader(idp)
                }
                idp != null
            }
        }
        if(kcl_opt != null && dpRes){
            return kcl_opt
        } else {
            Log.error("Unable to install {}:{}:{}", du.groupName, du.name, du.version)
            return null
        }
    }
}
