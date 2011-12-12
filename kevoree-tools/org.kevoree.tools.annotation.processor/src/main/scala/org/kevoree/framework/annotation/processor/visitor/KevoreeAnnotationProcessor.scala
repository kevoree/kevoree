/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework.annotation.processor.visitor

import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.PostAptChecker
import org.kevoree.tools.annotation.generator.KevoreeActivatorGenerator
import org.kevoree.tools.annotation.generator.KevoreeFactoryGenerator
import org.kevoree.tools.annotation.generator.KevoreeGenerator

import org.kevoree.framework._
import scala.collection.JavaConversions._
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic.Kind
import org.kevoree.annotation._
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class KevoreeAnnotationProcessor() extends javax.annotation.processing.AbstractProcessor {

  var options: java.util.HashMap[String, String] = _

  def getOptions = options

  def setOptions(s: java.util.HashMap[String, String]) = {
    options = s
  }

  lazy val env = processingEnv

  override def getSupportedOptions: java.util.Set[String] = {
    import scala.collection.JavaConversions._
    Set[String]()
  }

  override def getSupportedAnnotationTypes: java.util.Set[String] = {
    val stype = new java.util.HashSet[String]
    stype.add(classOf[org.kevoree.annotation.ChannelTypeFragment].getName)
    stype.add(classOf[org.kevoree.annotation.ComponentType].getName)
    stype.add(classOf[org.kevoree.annotation.Port].getName)
    stype.add(classOf[org.kevoree.annotation.ProvidedPort].getName)
    stype.add(classOf[org.kevoree.annotation.Provides].getName)
    stype.add(classOf[org.kevoree.annotation.Requires].getName)
    stype.add(classOf[org.kevoree.annotation.RequiredPort].getName)
    stype.add(classOf[org.kevoree.annotation.Start].getName)
    stype.add(classOf[org.kevoree.annotation.Stop].getName)
    stype.add(classOf[org.kevoree.annotation.Ports].getName)
    stype.add(classOf[org.kevoree.annotation.ThirdParties].getName)
    stype.add(classOf[org.kevoree.annotation.ThirdParty].getName)
    stype.add(classOf[org.kevoree.annotation.DictionaryAttribute].getName)
    stype.add(classOf[org.kevoree.annotation.DictionaryType].getName)
    stype.add(classOf[org.kevoree.annotation.ComponentFragment].getName)
    stype.add(classOf[org.kevoree.annotation.Library].getName)
    stype.add(classOf[org.kevoree.annotation.GroupType].getName)
    stype.add(classOf[org.kevoree.annotation.NodeType].getName)
    stype.add(classOf[org.kevoree.annotation.Slot].getName)
    stype.add(classOf[org.kevoree.annotation.SlotPort].getName)
    return stype
  }

  override def getSupportedSourceVersion: SourceVersion = {
    return SourceVersion.latest
  }

  def process(annotations: java.util.Set[_ <: TypeElement], roundEnv: RoundEnvironment): Boolean = {

    if (annotations.size() == 0) {
      return true
    }

    val root = KevoreeFactory.eINSTANCE.createContainerRoot
    LocalUtility.root = (root)
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.ComponentType]).foreach {
      typeDecl =>
        processComponentType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.ComponentType]), typeDecl.asInstanceOf[TypeElement], root)
    }
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.ChannelTypeFragment]).foreach {
      typeDecl =>
        processChannelType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.ChannelTypeFragment]), typeDecl.asInstanceOf[TypeElement], root)
    }
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.GroupType]).foreach {
      typeDecl =>
        processGroupType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.GroupType]), typeDecl.asInstanceOf[TypeElement], root)
    }
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.NodeType]).foreach {
      typeDecl =>
        processNodeType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.NodeType]), typeDecl.asInstanceOf[TypeElement], root)
    }

    //POST APT PROCESS CHECKER
    val checker: PostAptChecker = new PostAptChecker(root, env)
    val errorsInChecker = !checker.check
    KevoreeXmiHelper.save(LocalUtility.generateLibURI(options) + ".debug", root);

    if (!errorsInChecker) {
      //TODO SEPARATE MAVEN PLUGIN
      val nodeTypeNames = options.get("nodeTypeNames")
      val nodeTypeNameList: List[String] = nodeTypeNames.split(",").filter(r => r != null && r != "").toList
      nodeTypeNameList.foreach {
        targetNodeName =>
          KevoreeGenerator.generatePort(root, env.getFiler, targetNodeName);
          KevoreeFactoryGenerator.generateFactory(root, env.getFiler, targetNodeName);
          KevoreeActivatorGenerator.generateActivator(root, env.getFiler, targetNodeName);
      }
      env.getMessager.printMessage(Kind.OTHER, "Save Kevoree library")
      KevoreeXmiHelper.save(LocalUtility.generateLibURI(options), root);
      true
    } else {
      false
    }

  }


  def processNodeType(nodeTypeAnnotation: NodeType, typeDecl: TypeElement, root: ContainerRoot) = {
    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractNodeType].getName)
    typeDecl.accept(superTypeChecker, typeDecl)

    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(javax.lang.model.element.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true;
        env.getMessager.printMessage(Kind.WARNING, "NodeType bean ignored  " + typeDecl.getQualifiedName + ", reason=Declared as @NodeType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result) {

      val nodeType: org.kevoree.NodeType = root.getTypeDefinitions.find(td => td.getName == typeDecl.getSimpleName) match {
        case Some(found) => found.asInstanceOf[org.kevoree.NodeType]
        case None => KevoreeFactory.eINSTANCE.createNodeType
      }

      val nodeTypeName = typeDecl.getSimpleName
      nodeType.setName(nodeTypeName.toString)
      if (!isAbstract) {
        nodeType.setBean(typeDecl.getQualifiedName.toString)
        nodeType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      }


      root.addTypeDefinitions(nodeType)

      //RUN VISITOR
      typeDecl.accept(NodeTypeVisitor(nodeType, env, this), typeDecl)
    } else {
      env.getMessager.printMessage(Kind.WARNING, "NodeType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractNodeType].getName)
    }
  }


  def processGroupType(groupTypeAnnotation: GroupType, typeDecl: TypeElement, root: ContainerRoot) = {
    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractGroupType].getName)
    typeDecl.accept(superTypeChecker, typeDecl)

    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(javax.lang.model.element.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true;
        env.getMessager.printMessage(Kind.WARNING, "ComponentType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ComponentType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {

      val groupType = KevoreeFactory.eINSTANCE.createGroupType
      val groupName = typeDecl.getSimpleName
      groupType.setName(groupName.toString)
      groupType.setBean(typeDecl.getQualifiedName.toString)
      groupType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      root.addTypeDefinitions(groupType)

      //RUN VISITOR
      typeDecl.accept(GroupTypeVisitor(groupType, env, this), typeDecl)
    } else {
      env.getMessager.printMessage(Kind.WARNING, "GroupType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractGroupType].getName)
    }
  }


  def processChannelType(channelTypeAnnotation: org.kevoree.annotation.ChannelTypeFragment, typeDecl: TypeElement, root: ContainerRoot) = {

    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractChannelFragment].getName)
    typeDecl.accept(superTypeChecker, typeDecl)

    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(javax.lang.model.element.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true;
        env.getMessager.printMessage(Kind.WARNING, "ComponentType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ComponentType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {

      val channelType = KevoreeFactory.eINSTANCE.createChannelType
      val ctname = typeDecl.getSimpleName
      channelType.setName(ctname.toString)
      channelType.setBean(typeDecl.getQualifiedName.toString)
      channelType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      root.addTypeDefinitions(channelType)

      //RUN VISITOR
      typeDecl.accept(ChannelTypeFragmentVisitor(channelType, env, this), typeDecl)
    } else {
      env.getMessager.printMessage(Kind.WARNING, "ChannelFragment ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractChannelFragment].getName)
    }
  }

  def processComponentType(componentTypeAnnotation: org.kevoree.annotation.ComponentType, typeDecl: TypeElement, root: ContainerRoot) = {

    //Checks that the root KevoreeComponent is present in hierarchy.

    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractComponentType].getName)
    typeDecl.accept(superTypeChecker, typeDecl)
    //Prints a warning
    if (!superTypeChecker.result) {
      env.getMessager.printMessage(Kind.WARNING, "ComponentType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractComponentType].getName)
    }

    //Checks the Class is not Abstract
    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(javax.lang.model.element.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true;
        env.getMessager.printMessage(Kind.WARNING, "ComponentType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ComponentType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {
      val componentType = KevoreeFactory.eINSTANCE.createComponentType
      val ctname = typeDecl.getSimpleName
      componentType.setName(ctname.toString)
      componentType.setBean(typeDecl.getQualifiedName.toString)
      componentType.setFactoryBean(typeDecl.getQualifiedName + "Factory")

      root.addTypeDefinitions(componentType)
      //RUN VISITOR
      typeDecl.accept(ComponentDefinitionVisitor(componentType, env, this), typeDecl)
    }


  }


}
