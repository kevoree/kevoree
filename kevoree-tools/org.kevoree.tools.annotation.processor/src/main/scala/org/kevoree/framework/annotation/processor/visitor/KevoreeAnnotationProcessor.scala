/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kevoree.framework.annotation.processor.visitor

import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.PostAptChecker
import org.kevoree.tools.annotation.generator.{ThreadingMapping, KevoreeActivatorGenerator, KevoreeFactoryGenerator, KevoreeGenerator}

import org.kevoree.framework._
import scala.collection.JavaConversions._
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic.Kind
import org.kevoree.annotation._
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import java.util.HashSet

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

  private val threadProtectionAsked = new HashSet[String]


  def process(annotations: java.util.Set[_ <: TypeElement], roundEnv: RoundEnvironment): Boolean = {

    if (annotations.size() == 0) {
      return true
    }

    val root = LocalUtility.kevoreeFactory.createContainerRoot
    LocalUtility.root = (root)
    /* Look for reserved thread protection */
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.ReservedThread]).foreach {
      typeDecl =>
        threadProtectionAsked.add(typeDecl.getSimpleName.toString)
    }
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.ComponentType]).foreach {
      typeDecl =>
        processComponentType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.ComponentType]), typeDecl.asInstanceOf[TypeElement], root)
    }
    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.ChannelTypeFragment]).foreach {
      typeDecl =>

        processChannelType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.ChannelTypeFragment]), typeDecl.asInstanceOf[TypeElement], root)
    }

    // KevoreeXmiHelper.save(LocalUtility.generateLibURI(options) + ".beforeGTdebug", root);


    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.GroupType]).foreach {
      typeDecl =>
        processGroupType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.GroupType]), typeDecl.asInstanceOf[TypeElement], root)
    }

    roundEnv.getElementsAnnotatedWith(classOf[org.kevoree.annotation.NodeType]).foreach {
      typeDecl =>
        processNodeType(typeDecl.getAnnotation(classOf[org.kevoree.annotation.NodeType]), typeDecl.asInstanceOf[TypeElement], root)
    }

    //KevoreeXmiHelper.save(LocalUtility.generateLibURI(options) + ".beforeCheckerdebug", root);


    //POST APT PROCESS CHECKER
    val checker: PostAptChecker = new PostAptChecker(root, env)
    val errorsInChecker = !checker.check
    KevoreeXmiHelper.$instance.save(LocalUtility.generateLibURI(options) + ".debug", root);

    if (!errorsInChecker) {
      //TODO SEPARATE MAVEN PLUGIN
      val nodeTypeNames = options.get("nodeTypeNames")
      val nodeTypeNameList: List[String] = nodeTypeNames.split(",").filter(r => r != null && r != "").toList
      nodeTypeNameList.foreach {
        targetNodeName =>
          KevoreeGenerator.generatePort(root, env.getFiler, targetNodeName)
          //KevoreeFactoryGenerator.generateFactory(root, env.getFiler, targetNodeName)
          //KevoreeActivatorGenerator.generateActivator(root, env.getFiler, targetNodeName)
      }
      env.getMessager.printMessage(Kind.OTHER, "Save Kevoree library")
      KevoreeXmiHelper.$instance.save(LocalUtility.generateLibURI(options), root);
      true
    } else {
      false
    }

  }


  def processNodeType(nodeTypeAnnotation: org.kevoree.annotation.NodeType, typeDecl: TypeElement, root: ContainerRoot) = {
    //Checks that the root AbstractNodeType is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractNodeType].getName)
    typeDecl.accept(superTypeChecker, typeDecl)

    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(javax.lang.model.element.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true
        env.getMessager.printMessage(Kind.WARNING, "NodeType bean ignored  " + typeDecl.getQualifiedName + ", reason=Declared as @NodeType but is actually ABSTRACT. Should be either concrete or @NodeFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result) {
      val nodeTypeName = typeDecl.getSimpleName
      val nodeType: org.kevoree.NodeType = root.findByPath("typeDefinitions[" + nodeTypeName + "]", classOf[org.kevoree.NodeType]) match {
        case found : org.kevoree.NodeType => found
        case null => {
          val nodeType = LocalUtility.kevoreeFactory.createNodeType
          nodeType.setName(nodeTypeName.toString)
          root.addTypeDefinitions(nodeType)
          nodeType
        }
      }

      if (!isAbstract) {
        nodeType.setBean(typeDecl.getQualifiedName.toString)
        nodeType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      }


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
        isAbstract = true
        env.getMessager.printMessage(Kind.WARNING, "GroupType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @GroupType but is actually ABSTRACT. Should be either concrete or @GroupFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {
      val groupName = typeDecl.getSimpleName
      val groupType: org.kevoree.GroupType = root.findByPath("typeDefinitions[" + groupName + "]", classOf[org.kevoree.GroupType]) match {
        case null => {
          val groupType = LocalUtility.kevoreeFactory.createGroupType
          groupType.setName(groupName.toString)
          root.addTypeDefinitions(groupType)
          groupType
        }
        case td : org.kevoree.GroupType => {
          td
        }
      }

      groupType.setBean(typeDecl.getQualifiedName.toString)
      groupType.setFactoryBean(typeDecl.getQualifiedName + "Factory")

      //RUN VISITOR
      typeDecl.accept(GroupTypeVisitor(groupType, env, this), typeDecl)
    } else {
      env.getMessager.printMessage(Kind.WARNING, "GroupType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractGroupType].getName)
    }
  }


  def processChannelType(channelTypeAnnotation: org.kevoree.annotation.ChannelTypeFragment, typeDecl: TypeElement, root: ContainerRoot) = {

    ThreadingMapping.getMappings.put((typeDecl.getSimpleName.toString, typeDecl.getSimpleName.toString), channelTypeAnnotation.theadStrategy())

    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractChannelFragment].getName)
    typeDecl.accept(superTypeChecker, typeDecl)

    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(javax.lang.model.element.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true
        env.getMessager.printMessage(Kind.WARNING, "ChannelType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ChannelFragment but is actually ABSTRACT")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {
      val channelName = typeDecl.getSimpleName
      val channelType: org.kevoree.ChannelType = root.findByPath("typeDefinitions[" + channelName + "]", classOf[org.kevoree.ChannelType]) match {
        case null => {
          val channelType = LocalUtility.kevoreeFactory.createChannelType
          channelType.setName(channelName.toString)
          root.addTypeDefinitions(channelType)
          channelType
        }
        case td : org.kevoree.ChannelType => {
          td
        }
      }

      channelType.setBean(typeDecl.getQualifiedName.toString)
      channelType.setFactoryBean(typeDecl.getQualifiedName + "Factory")

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
        isAbstract = true
        env.getMessager.printMessage(Kind.WARNING, "ComponentType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ComponentType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {
      val componentName = typeDecl.getSimpleName
      val componentType: org.kevoree.ComponentType = root.findByPath("typeDefinitions[" + componentName + "]", classOf[org.kevoree.ComponentType]) match {
        case null => {
          val componentType = LocalUtility.kevoreeFactory.createComponentType
          componentType.setName(componentName.toString)
          root.addTypeDefinitions(componentType)
          componentType
        }
        case td : org.kevoree.ComponentType => {
          td
        }
      }

      componentType.setBean(typeDecl.getQualifiedName.toString)
      componentType.setFactoryBean(typeDecl.getQualifiedName + "Factory")

      //RUN VISITOR
      val cvisitor = ComponentDefinitionVisitor(componentType, env, this)
      typeDecl.accept(cvisitor, typeDecl)
      cvisitor.doAnnotationPostProcess(componentType)

    }
  }
}
