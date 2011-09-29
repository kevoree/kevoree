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

import com.sun.mirror.apt.AnnotationProcessor
import com.sun.mirror.apt.AnnotationProcessorEnvironment
import com.sun.mirror.declaration.TypeDeclaration
import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.annotation.processor.KevoreeXmiHelper
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.PostAptChecker
import org.kevoree.tools.annotation.generator.KevoreeActivatorGenerator
import org.kevoree.tools.annotation.generator.KevoreeFactoryGenerator
import org.kevoree.tools.annotation.generator.KevoreeGenerator
import scala.collection.JavaConversions._
import org.kevoree.annotation.{GroupType, ChannelTypeFragment, ComponentType, NodeType}
import org.kevoree.framework.{AbstractNodeType, AbstractGroupType, AbstractChannelFragment, AbstractComponentType}

class KevoreeAnnotationProcessor(env: AnnotationProcessorEnvironment) extends AnnotationProcessor {

  def process() = {

    val root = KevoreeFactory.eINSTANCE.createContainerRoot();
    LocalUtility.root=(root)
    env.getTypeDeclarations().foreach {
      typeDecl =>

      //PROCESS COMPONENT TYPE
        val ctAnnotation = typeDecl.getAnnotation(classOf[ComponentType]);
        if (ctAnnotation != null) {
          processComponentType(ctAnnotation, typeDecl, root)
        }
        //PROCESS CHANNEL TYPE
        val channelTypeAnnotation = typeDecl.getAnnotation(classOf[ChannelTypeFragment]);
        if (channelTypeAnnotation != null) {
          processChannelType(channelTypeAnnotation, typeDecl, root)
        }
        //GROUPTYPE
        val groupTypeAnnotation = typeDecl.getAnnotation(classOf[GroupType]);
        if (groupTypeAnnotation != null) {
          processGroupType(groupTypeAnnotation, typeDecl, root)
        }
        //NODETYPE
        val nodeTypeAnnotation = typeDecl.getAnnotation(classOf[NodeType]);
        if (nodeTypeAnnotation != null) {
          processNodeType(nodeTypeAnnotation, typeDecl, root)
        }
    }


    //POST APT PROCESS CHECKER
    val checker: PostAptChecker = new PostAptChecker(root, env)
    val errorsInChecker = !checker.check


    if (!errorsInChecker) {
      //TODO SEPARATE MAVEN PLUGIN

      val nodeTypeNames = env.getOptions.find({
        op => op._1.contains("nodeTypeNames")
      }).getOrElse {
        ("key=", "")
      }._1.split('=').toList.get(1)
      val nodeTypeNameList: List[String] = nodeTypeNames.split(",").filter(r => r != null && r != "").toList
      nodeTypeNameList.foreach {
        targetNodeName =>
          KevoreeGenerator.generatePort(root, env.getFiler,targetNodeName);
          KevoreeFactoryGenerator.generateFactory(root, env.getFiler,targetNodeName);
          KevoreeActivatorGenerator.generateActivator(root, env.getFiler,targetNodeName);
      }

      KevoreeXmiHelper.save(LocalUtility.generateLibURI(env), root);
    }
  }


  def processNodeType(nodeTypeAnnotation: NodeType, typeDecl: TypeDeclaration, root: ContainerRoot) = {
    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractNodeType].getName)
    typeDecl.accept(superTypeChecker)
    if (superTypeChecker.result) {

      val nodeType = KevoreeFactory.eINSTANCE.createNodeType
      val nodeTypeName = typeDecl.getSimpleName
      nodeType.setName(nodeTypeName)
      nodeType.setBean(typeDecl.getQualifiedName)
      nodeType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      root.getTypeDefinitions.add(nodeType)

      //RUN VISITOR
      typeDecl.accept(NodeTypeVisitor(nodeType, env))
    } else {
      env.getMessager.printWarning("NodeType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractNodeType].getName)
    }
  }


  def processGroupType(groupTypeAnnotation: GroupType, typeDecl: TypeDeclaration, root: ContainerRoot) = {
    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractGroupType].getName)
    typeDecl.accept(superTypeChecker)
    if (superTypeChecker.result) {

      val groupType = KevoreeFactory.eINSTANCE.createGroupType
      val groupName = typeDecl.getSimpleName
      groupType.setName(groupName)
      groupType.setBean(typeDecl.getQualifiedName)
      groupType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      root.getTypeDefinitions.add(groupType)

      //RUN VISITOR
      typeDecl.accept(GroupTypeVisitor(groupType, env))
    } else {
      env.getMessager.printWarning("GroupType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractGroupType].getName)
    }
  }


  def processChannelType(channelTypeAnnotation: ChannelTypeFragment, typeDecl: TypeDeclaration, root: ContainerRoot) = {

    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractChannelFragment].getName)
    typeDecl.accept(superTypeChecker)
    if (superTypeChecker.result) {

      val channelType = KevoreeFactory.eINSTANCE.createChannelType();
      val ctname = typeDecl.getSimpleName
      channelType.setName(ctname)
      channelType.setBean(typeDecl.getQualifiedName)
      channelType.setFactoryBean(typeDecl.getQualifiedName + "Factory")
      root.getTypeDefinitions.add(channelType)

      //RUN VISITOR
      typeDecl.accept(ChannelTypeFragmentVisitor(channelType, env))
    } else {
      env.getMessager.printWarning("ChannelFragment ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractChannelFragment].getName)
    }
  }

  def processComponentType(componentTypeAnnotation: ComponentType, typeDecl: TypeDeclaration, root: ContainerRoot) = {

    //Checks that the root KevoreeComponent is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractComponentType].getName)
    typeDecl.accept(superTypeChecker)
    //Prints a warning
    if (!superTypeChecker.result) {
      env.getMessager.printWarning("ComponentType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractComponentType].getName)
    }

    //Checks the Class is not Abstract
    var isAbstract = false
    typeDecl.getModifiers.find(mod => mod.equals(com.sun.mirror.declaration.Modifier.ABSTRACT)) match {
      case Some(s) => {
        isAbstract = true;
        env.getMessager.printWarning("ComponentType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ComponentType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
      }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {
      val componentType = KevoreeFactory.eINSTANCE.createComponentType();
      val ctname = typeDecl.getSimpleName
      componentType.setName(ctname)
      componentType.setBean(typeDecl.getQualifiedName)
      componentType.setFactoryBean(typeDecl.getQualifiedName + "Factory")

      root.getTypeDefinitions.add(componentType)
      //RUN VISITOR
      typeDecl.accept(ComponentDefinitionVisitor(componentType, env))
    }


  }


}
