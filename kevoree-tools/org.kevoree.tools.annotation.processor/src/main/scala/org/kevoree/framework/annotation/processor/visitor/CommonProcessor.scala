package org.kevoree.framework.annotation.processor.visitor

import org.kevoree.{NamedElement, TypeDefinition}
import javax.lang.model.element.{Modifier, Element, ElementVisitor, TypeElement}
import org.kevoree.framework.annotation.processor.visitor.sub._
import scala.collection.JavaConversions._
import javax.annotation.processing.ProcessingEnvironment

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/09/13
 * Time: 18:06
 *
 * @author Erwan Daubert
 * @version 1.0
 */
trait CommonProcessor extends DeployUnitProcessor
with DictionaryProcessor
with LibraryProcessor
with ThirdPartyProcessor
with TypeDefinitionProcessor {

  var typeDefinition: TypeDefinition
  var env: ProcessingEnvironment
  var rootVisitor: KevoreeAnnotationProcessor
  var elementVisitor: ElementVisitor[Any, Element]
  var annotationType : Class[_ <: java.lang.annotation.Annotation]
  var typeDefinitionType : Class[_ <: TypeDefinition]

  def processInterface(typeDecl: TypeElement) {
    typeDecl.getInterfaces.foreach {
      it =>
        it match {
          case dt: javax.lang.model.`type`.DeclaredType => {
            val annotFragment = dt.asElement().getAnnotation(annotationType)
            if (annotFragment != null) {
              dt.asElement().accept(elementVisitor, dt.asElement())
              defineAsSuperType(typeDefinition, dt.asElement().getSimpleName.toString, typeDefinitionType, true)
            } else {
              commonGenericProcess(dt.asElement().asInstanceOf[TypeElement])
            }
          }
          case _ =>
        }
    }
  }

  def processSuperClass(typeDecl: TypeElement) {
    typeDecl.getSuperclass match {
      case dt: javax.lang.model.`type`.DeclaredType => {
        val an = dt.asElement().getAnnotation(annotationType)
        if (an != null) {
          dt.asElement().accept(elementVisitor, dt.asElement())
          val isAbstract = dt.asElement().getModifiers.contains(Modifier.ABSTRACT)
          defineAsSuperType(typeDefinition, dt.asElement().getSimpleName.toString, typeDefinitionType, isAbstract)
        }  else {
          commonGenericProcess(dt.asElement().asInstanceOf[TypeElement])
        }
      }
      case _ =>
    }
  }

  def commonGenericProcess(typeDecl: TypeElement) {
    processInterface(typeDecl)
    processSuperClass(typeDecl)
    processDictionary(typeDefinition, typeDecl)
    processLibrary(typeDefinition, typeDecl)
  }

  def commonProcess(typeDecl: TypeElement) {
    commonGenericProcess(typeDecl)
    processDeployUnit(typeDefinition, typeDecl, env, rootVisitor.getOptions)
    processThirdParty(typeDefinition, typeDecl, env, rootVisitor)

  }
}
