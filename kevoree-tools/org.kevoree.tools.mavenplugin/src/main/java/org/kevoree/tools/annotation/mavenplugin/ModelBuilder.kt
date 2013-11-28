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
import org.kevoree.annotation.Param
import org.kevoree.annotation.Output
import org.kevoree.annotation.Input

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

    fun deepMethods(clazz: CtClass, factory: KevoreeFactory, currentTypeDefinition: TypeDefinition) {
        for(method in clazz.getDeclaredMethods()?.iterator()){


            for(annotation in method.getAnnotations()?.iterator()){
                when(annotation) {
                    is Input -> {
                        if(currentTypeDefinition is org.kevoree.ComponentType){
                            var providedPortRef = factory.createPortTypeRef()
                            providedPortRef.name = method.getName()
                            providedPortRef.optional = annotation.optional()
                            currentTypeDefinition.addProvided(providedPortRef)
                        }
                    }
                    else -> {
                        //noop
                    }
                }
            }
        }
    }

    fun deepFields(clazz: CtClass, factory: KevoreeFactory, currentTypeDefinition: TypeDefinition) {
        for(field in clazz.getDeclaredFields()?.iterator()){
            for(annotation in field.getAnnotations()?.iterator()){
                when(annotation) {
                    is Output -> {
                        if(currentTypeDefinition is org.kevoree.ComponentType){
                            var requiredPortRef = factory.createPortTypeRef()
                            requiredPortRef.name = field.getName()
                            requiredPortRef.optional = annotation.optional()
                            currentTypeDefinition.addRequired(requiredPortRef)
                        }
                    }
                    is Param -> {
                        var dicAtt = factory.createDictionaryAttribute()
                        if(currentTypeDefinition.dictionaryType == null){
                            currentTypeDefinition.dictionaryType = factory.createDictionaryType()
                        }
                        dicAtt.name = field.getName()
                        dicAtt.datatype = field.getType()!!.getName()
                        dicAtt.optional = annotation.optional()
                        dicAtt.fragmentDependant = annotation.fragmentDependent()
                        currentTypeDefinition.dictionaryType!!.addAttributes(dicAtt)
                    }
                    else -> {
                        //noop
                    }
                }
            }
        }
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
                deepFields(clazz, factory, groupType)
            }
            is ChannelType -> {
                var channelType = factory.createChannelType();
                channelType.version = du.version
                channelType.name = clazz.getSimpleName()
                channelType.bean = clazz.getName()
                root.addTypeDefinitions(channelType)
                channelType.deployUnit = du
                deepFields(clazz, factory, channelType)
            }
            is ComponentType -> {
                var componentType = factory.createComponentType();
                componentType.version = du.version
                componentType.name = clazz.getSimpleName()
                componentType.bean = clazz.getName()
                root.addTypeDefinitions(componentType)
                componentType.deployUnit = du
                deepFields(clazz, factory, componentType)
                deepMethods(clazz, factory, componentType)
            }
            is NodeType -> {
                var nodeType = factory.createNodeType();
                nodeType.version = du.version
                nodeType.name = clazz.getSimpleName()
                nodeType.bean = clazz.getName()
                root.addTypeDefinitions(nodeType)
                nodeType.deployUnit = du
                deepFields(clazz, factory, nodeType)
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
