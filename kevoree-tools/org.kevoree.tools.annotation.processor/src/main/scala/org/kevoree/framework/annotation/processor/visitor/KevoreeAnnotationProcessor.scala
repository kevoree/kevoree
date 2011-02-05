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
import org.kevoree.annotation.ChannelTypeFragment
import org.kevoree.annotation.ComponentType
import org.kevoree.framework.annotation.processor.KevoreeXmiHelper
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.PostAptChecker
import org.kevoree.tools.annotation.generator.KevoreeActivatorGenerator
import org.kevoree.tools.annotation.generator.KevoreeFactoryGenerator
import org.kevoree.tools.annotation.generator.KevoreeGenerator
import scala.collection.JavaConversions._
import org.kevoree.framework.{AbstractChannelFragment, AbstractComponentType}

class KevoreeAnnotationProcessor(env: AnnotationProcessorEnvironment) extends AnnotationProcessor {

  def process() = {

    var root = KevoreeFactory.eINSTANCE.createContainerRoot();
    LocalUtility.root_=(root)
    env.getTypeDeclarations().foreach{
      typeDecl =>
      var ctAnnotation = typeDecl.getAnnotation(classOf[ComponentType]);
      if (ctAnnotation != null) {
        processComponentType(ctAnnotation, typeDecl, root)
      }

      var channelTypeAnnotation = typeDecl.getAnnotation(classOf[ChannelTypeFragment]);
      if (channelTypeAnnotation != null) {
        processChannelType(channelTypeAnnotation, typeDecl, root)
      }
      //TODO

    }

    //POST APT PROCESS CHECKER
    var checker: PostAptChecker = new PostAptChecker(root, env)
    val errorsInChecker = !checker.check


    if(!errorsInChecker) {
      //TODO SEPARATE MAVEN PLUGIN
      KevoreeGenerator.generatePort(root, env.getFiler());
      KevoreeFactoryGenerator.generateFactory(root, env.getFiler());
      KevoreeActivatorGenerator.generateActivator(root, env.getFiler());

      System.out.println("Saving to " + LocalUtility.generateLibURI(env));
      KevoreeXmiHelper.save(LocalUtility.generateLibURI(env), root);
    }
  }


  def processChannelType(channelTypeAnnotation: ChannelTypeFragment, typeDecl: TypeDeclaration, root: ContainerRoot) = {

    //Checks that the root KevoreeChannelFragment is present in hierarchy.
    val superTypeChecker = new SuperTypeValidationVisitor(classOf[AbstractChannelFragment].getName)
    typeDecl.accept(superTypeChecker)
    if (superTypeChecker.result) {

      var channelType = KevoreeFactory.eINSTANCE.createChannelType();
      var ctname = channelTypeAnnotation.name
      if (ctname.equals("empty")) {
        ctname = typeDecl.getSimpleName
      }
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
    if( !superTypeChecker.result) {
      env.getMessager.printWarning("ComponentType ignored " + typeDecl.getQualifiedName + " , reason=Must extend " + classOf[AbstractComponentType].getName)
    }

    //Checks the Class is not Abstract
    var isAbstract = false
    typeDecl.getModifiers.find( mod => mod.equals(com.sun.mirror.declaration.Modifier.ABSTRACT)) match {
      case Some(s) => {
          isAbstract = true;
          env.getMessager.printWarning("ComponentType ignored " + typeDecl.getQualifiedName + ", reason=Declared as @ComponentType but is actually ABSTRACT. Should be either concrete or @ComponentFragment.")
        }
      case None =>
    }

    if (superTypeChecker.result && !isAbstract) {
      var componentType = KevoreeFactory.eINSTANCE.createComponentType();
      var ctname = componentTypeAnnotation.name
      if (ctname.equals("empty")) {
        ctname = typeDecl.getSimpleName
      }
      componentType.setName(ctname)
      componentType.setBean(typeDecl.getQualifiedName)
      componentType.setFactoryBean(typeDecl.getQualifiedName + "Factory")

      root.getTypeDefinitions.add(componentType)
      //RUN VISITOR
      typeDecl.accept(ComponentDefinitionVisitor(componentType, env))
    }


  }


}
