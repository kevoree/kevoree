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
import org.kevoree.annotation.KevoreeInject
import org.kevoree.api.ModelService
import org.kevoree.api.BootstrapService
import org.kevoree.api.KevScriptService
import org.kevoree.api.Context
import org.kevoree.api.ChannelContext

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
                            if(method.getParameterTypes()?.size!! > 1){
                                throw Exception("Input annotation should be used only on method with 0 or 1 parameter ${method.getName()}")
                            } else {
                                if(method.getParameterTypes()?.size == 1){
                                    var firstParam = method.getParameterTypes()!!.get(0)
                                    if(!firstParam.getName().equals(javaClass<Object>().getName())){
                                        throw Exception("Input method only support Object type parameter ${method.getName()}");
                                    }
                                }
                            }
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
                    is KevoreeInject -> {
                        when(field.getType()!!.getName()) {
                            javaClass<ModelService>().getName() -> {

                            }
                            javaClass<BootstrapService>().getName() -> {

                            }
                            javaClass<KevScriptService>().getName() -> {

                            }
                            javaClass<Context>().getName() -> {

                            }
                            javaClass<ChannelContext>().getName() -> {

                            }
                            else -> {
                                throw Exception("KevoreeInject annotation is only suitable for following types : ModelService,BootstrapService,KevScriptService,Context,ChannelContext : currently found : " + field.getType()!!.getName())
                            }
                        }
                    }
                    is Output -> {
                        if(!field.getType()!!.getName().equals(javaClass<org.kevoree.api.Port>().getName())){
                            throw Exception("Output port field must of type of " + javaClass<org.kevoree.api.Port>().getName())
                        }
                        if(currentTypeDefinition is org.kevoree.ComponentType){
                            var requiredPortRef = factory.createPortTypeRef()
                            requiredPortRef.name = field.getName()
                            requiredPortRef.optional = annotation.optional()
                            currentTypeDefinition.addRequired(requiredPortRef)
                        }
                    }
                    is Param -> {
                        //verify param type
                        when(field.getType()!!.getName()) {
                            javaClass<java.lang.String>().getName() -> {
                            }
                            javaClass<java.lang.Float>().getName() -> {
                            }
                            javaClass<java.lang.Integer>().getName() -> {
                            }
                            javaClass<java.lang.Double>().getName() -> {
                            }
                            javaClass<java.lang.Boolean>().getName() -> {
                            }
                            javaClass<java.lang.Long>().getName() -> {
                            }
                            else -> {
                                if(!field.getType()!!.isPrimitive()){
                                    throw Exception("Param annotation is only applicable on field of type String,Long,Double,Float,Integer, current " + field.getType()?.getName())
                                }
                            }
                        }
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
