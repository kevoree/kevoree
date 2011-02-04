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
import com.sun.mirror.declaration.AnnotationTypeDeclaration
import com.sun.mirror.declaration.Declaration
import com.sun.mirror.declaration.TypeDeclaration
import org.kevoree.KevoreeFactory
import org.kevoree.ChannelType
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

class KevoreeAnnotationProcessor(env : AnnotationProcessorEnvironment) extends AnnotationProcessor {

  def process()={

    var root = KevoreeFactory.eINSTANCE.createContainerRoot();
    LocalUtility.root_=(root)
    env.getTypeDeclarations().foreach{typeDecl=>
      var ctAnnotation = typeDecl.getAnnotation(classOf[ComponentType]);
      if(ctAnnotation != null){
        processComponentType(ctAnnotation,typeDecl,root)
      }

      var channelTypeAnnotation = typeDecl.getAnnotation(classOf[ChannelTypeFragment]);
      if(channelTypeAnnotation != null){
        processChannelType(channelTypeAnnotation,typeDecl,root)
      }
      //TODO

    }

    //POST APT PROCESS CHECKER
    var checker : PostAptChecker = new PostAptChecker(root)
    if( !checker.check){printf("PostAptChecker returned errors. Process aborted."); System.exit(1)}


    //TODO SEPARATE MAVEN PLUGIN
    KevoreeGenerator.generatePort(root, env.getFiler());
    KevoreeFactoryGenerator.generateFactory(root, env.getFiler());
    KevoreeActivatorGenerator.generateActivator(root, env.getFiler());

    System.out.println("Saving to "+ LocalUtility.generateLibURI(env));
    KevoreeXmiHelper.save(LocalUtility.generateLibURI(env), root);
  }



  def processChannelType(channelTypeAnnotation : ChannelTypeFragment,typeDecl : TypeDeclaration,root : ContainerRoot) = {
    var channelType = KevoreeFactory.eINSTANCE.createChannelType();
    var ctname = channelTypeAnnotation.name
    if(ctname.equals("empty")){
      ctname = typeDecl.getSimpleName
    }
    channelType.setName(ctname)
    channelType.setBean(typeDecl.getQualifiedName)
    channelType.setFactoryBean(typeDecl.getQualifiedName+"Factory")
    root.getTypeDefinitions.add(channelType)

    //RUN VISITOR
    typeDecl.accept(ChannelTypeFragmentVisitor(channelType,env))
  }

  def processComponentType(componentTypeAnnotation : ComponentType,typeDecl : TypeDeclaration,root : ContainerRoot) = {
    var componentType = KevoreeFactory.eINSTANCE.createComponentType();
    var ctname = componentTypeAnnotation.name
    if(ctname.equals("empty")){
      ctname = typeDecl.getSimpleName
    }
    componentType.setName(ctname)
    componentType.setBean(typeDecl.getQualifiedName)
    componentType.setFactoryBean(typeDecl.getQualifiedName+"Factory")
    
    root.getTypeDefinitions.add(componentType)
    //RUN VISITOR
    typeDecl.accept(ComponentDefinitionVisitor(componentType,env))

  }




}
