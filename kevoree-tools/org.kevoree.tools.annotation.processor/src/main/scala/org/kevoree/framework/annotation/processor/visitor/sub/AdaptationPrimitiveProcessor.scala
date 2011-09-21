package org.kevoree.framework.annotation.processor.visitor.sub

import com.sun.mirror.declaration.TypeDeclaration
import com.sun.mirror.apt.AnnotationProcessorEnvironment
import org.kevoree.annotation.PrimitiveCommand
import org.kevoree.{KevoreeFactory, AdaptationPrimitiveType, ContainerRoot, NodeType}
import org.kevoree.tools.annotation.generator.AdaptationPrimitiveMapping

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/09/11
 * Time: 09:30
 */

trait AdaptationPrimitiveProcessor {

  val builder = new StringBuilder

  def processPrimitiveCommand (typeDef: NodeType, classdef: TypeDeclaration, env: AnnotationProcessorEnvironment) {
    //Collects all primitive command annotations and creates a list
    var primitiveCommandAnnotations: List[org.kevoree.annotation.PrimitiveCommand] = Nil

    if (classdef.getAnnotation(classOf[org.kevoree.annotation.PrimitiveCommands]) != null) {
      primitiveCommandAnnotations = primitiveCommandAnnotations ++
        classdef.getAnnotation(classOf[org.kevoree.annotation.PrimitiveCommands]).value.toList

      //For each annotation in the list
      primitiveCommandAnnotations.foreach {
        primitiveCommandAnnotation =>
        // check if the AdaptationPrimitive name is defined once
          if (primitiveCommandAnnotations.find(a => a.name() == primitiveCommandAnnotation.name()).size == 1) {
            primitiveCommandAnnotation.clazz().getInterfaces
              .filter(i => i.getName == classOf[org.kevoree.framework.PrimitiveCommand].getName) match {
              case None => {
                // generate a compilation error
                env.getMessager.printError("Primitive command " + primitiveCommandAnnotation.clazz().getName + " in " +
                  typeDef.getName + " doesn't implement required interface " +
                  classOf[org.kevoree.framework.PrimitiveCommand].getName)
              }
              case Some(i) => {
                // add type on lib
                val primitiveType: AdaptationPrimitiveType = KevoreeFactory.eINSTANCE.createAdaptationPrimitiveType()
                addPrimitiveType(typeDef, primitiveType)
                // register mapping for getPrimivite() fucntion on NodeType
                AdaptationPrimitiveMapping.addMapping (typeDef, primitiveCommandAnnotation.name(), primitiveCommandAnnotation.clazz().getName)

              }
            }
          } else {
            // generate a compilation error
            env.getMessager
              .printError("Primitive command " + primitiveCommandAnnotation.name() + " is defined more than once !")
          }
      }
    }
  }

  private def addPrimitiveType (typeDef: NodeType, primitiveType: AdaptationPrimitiveType) {
    val root = typeDef.eContainer.asInstanceOf[ContainerRoot]
    root.getAdaptationPrimitiveTypes.add(primitiveType)
    typeDef.getManagedPrimitiveTypes.add(primitiveType)
  }
}