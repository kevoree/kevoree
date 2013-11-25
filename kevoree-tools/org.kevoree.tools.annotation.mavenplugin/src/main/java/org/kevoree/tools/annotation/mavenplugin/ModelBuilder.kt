package org.kevoree.tools.annotation.mavenplugin

import org.kevoree.annotation.NodeType
import javassist.CtClass
import org.kevoree.KevoreeFactory
import org.kevoree.DeployUnit
import org.kevoree.ContainerRoot
import org.kevoree.annotation.Library
import org.kevoree.TypeDefinition
import org.kevoree.annotation.ComponentType
import org.kevoree.annotation.ChannelType
import org.kevoree.annotation.GroupType

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 24/11/2013
 * Time: 18:02
 */

object ModelBuilder {

    private fun addLibrary(libName: String, typeDef: TypeDefinition, root: ContainerRoot, factory: KevoreeFactory) {
        var lib = root.findLibrariesByID(libName)
        if(lib == null){
            lib = factory.createTypeLibrary()
            lib!!.name = libName
            root.addLibraries(lib!!)
        }
        lib!!.addSubTypes(typeDef)
    }

    fun process(elem: Any, clazz: CtClass, factory: KevoreeFactory, du: DeployUnit, root: ContainerRoot) {

        when(elem) {
            is GroupType -> {
                var groupType = factory.createGroupType();
                groupType.version = du.version
                groupType.name = clazz.getSimpleName()
                groupType.bean = clazz.getName()
                root.addTypeDefinitions(groupType)
                groupType.deployUnit = du
            }
            is ChannelType -> {
                var channelType = factory.createChannelType();
                channelType.version = du.version
                channelType.name = clazz.getSimpleName()
                channelType.bean = clazz.getName()
                root.addTypeDefinitions(channelType)
                channelType.deployUnit = du
            }
            is ComponentType -> {
                var componentType = factory.createComponentType();
                componentType.version = du.version
                componentType.name = clazz.getSimpleName()
                componentType.bean = clazz.getName()
                root.addTypeDefinitions(componentType)
                componentType.deployUnit = du
            }
            is NodeType -> {
                var nodeType = factory.createNodeType();
                nodeType.version = du.version
                nodeType.name = clazz.getSimpleName()
                nodeType.bean = clazz.getName()
                root.addTypeDefinitions(nodeType)
                nodeType.deployUnit = du
            }
            is Library -> {
                for(typeDef in root.typeDefinitions){
                    if(typeDef.name == clazz.getSimpleName() && typeDef.version == du.version){
                        if(elem.name() != null){
                            addLibrary(elem.name()!!, typeDef, root, factory)
                        }
                        var libs = elem.names()
                        if(libs != null){
                            for(i in 0..libs!!.size - 1){
                                addLibrary(libs!!.get(i), typeDef, root, factory)
                            }
                        }
                    } else {
                        //Does not existeyep :-)
                        println("Please put Library annotation after NodeType declaration")
                    }
                }
            }
            else -> {
                // println(elem)
            }
        }

    }

}
