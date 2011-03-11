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

import org.kevoree.ComponentType
import com.sun.mirror.apt.AnnotationProcessorEnvironment
import com.sun.mirror.declaration.ClassDeclaration
import com.sun.mirror.declaration.InterfaceDeclaration
import com.sun.mirror.declaration.MethodDeclaration
import com.sun.mirror.declaration.TypeDeclaration
import com.sun.mirror.util.SimpleDeclarationVisitor
import org.kevoree.framework.annotation.processor.visitor.sub.DeployUnitProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.DictionaryProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.LibraryProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.LifeCycleMethodProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.PortMappingProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.ProvidedPortProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.RequiredPortProcessor
import org.kevoree.framework.annotation.processor.visitor.sub.ThirdPartyProcessor
import scala.collection.JavaConversions._

case class ComponentDefinitionVisitor(componentType : ComponentType,env : AnnotationProcessorEnvironment)
extends SimpleDeclarationVisitor
   with ProvidedPortProcessor
   with RequiredPortProcessor
   with ThirdPartyProcessor
   with DeployUnitProcessor
   with DictionaryProcessor
   with PortMappingProcessor
   with LibraryProcessor
   with LifeCycleMethodProcessor
{

  override def visitClassDeclaration(classdef : ClassDeclaration) = {
    if(classdef.getSuperclass != null){
      var annotFragment = classdef.getSuperclass.getDeclaration.getAnnotation(classOf[org.kevoree.annotation.ComponentFragment])
      if(annotFragment != null){
        classdef.getSuperclass.getDeclaration.accept(this)
      }
    }
    commonProcess(classdef)
  }

  override def visitInterfaceDeclaration(interfaceDecl : InterfaceDeclaration) = {
    commonProcess(interfaceDecl)
  }


  def commonProcess(typeDecl : TypeDeclaration) = {
    typeDecl.getSuperinterfaces.foreach{it=>
      var annotFragment = it.getDeclaration.getAnnotation(classOf[org.kevoree.annotation.ComponentFragment])
      it.getDeclaration.accept(this)
    }
    processLibrary(componentType,typeDecl)
    processDictionary(componentType,typeDecl)
    processDeployUnit(componentType,typeDecl,env)
    processThirdParty(componentType,typeDecl,env)
    processProvidedPort(componentType,typeDecl,env)
    processRequiredPort(componentType,typeDecl,env)

    typeDecl.getMethods().foreach{method => method.accept(this) }

  }

  override def visitMethodDeclaration(methoddef : MethodDeclaration) = {
    processPortMapping(componentType,methoddef,env)
    processLifeCycleMethod(componentType,methoddef)
  }




}
