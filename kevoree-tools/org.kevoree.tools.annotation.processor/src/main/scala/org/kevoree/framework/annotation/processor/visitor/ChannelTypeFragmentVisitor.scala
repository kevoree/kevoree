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

import com.sun.mirror.apt.AnnotationProcessorEnvironment
import com.sun.mirror.util.SimpleDeclarationVisitor

import sub._
import com.sun.mirror.declaration.{TypeDeclaration, InterfaceDeclaration, ClassDeclaration, MethodDeclaration}
import org.kevoree.{ComponentType, ChannelType}

case class ChannelTypeFragmentVisitor(channelType: ChannelType, env: AnnotationProcessorEnvironment)
  extends SimpleDeclarationVisitor
  with DeployUnitProcessor
  with DictionaryProcessor
  with LibraryProcessor
  with ThirdPartyProcessor
  with LifeCycleMethodProcessor
  with TypeDefinitionProcessor {

  override def visitClassDeclaration(classdef: ClassDeclaration) {
    if (classdef.getSuperclass != null) {
      val annotFragment = classdef.getSuperclass.getDeclaration.getAnnotation(classOf[org.kevoree.annotation.ComponentFragment])
      if (annotFragment != null) {
        classdef.getSuperclass.getDeclaration.accept(this)
        defineAsSuperType(channelType, classdef.getSuperclass.getDeclaration.getSimpleName, classOf[ChannelType])
      }
    }

    commonProcess(classdef)
  }

  override def visitInterfaceDeclaration(interfaceDecl: InterfaceDeclaration) {
    commonProcess(interfaceDecl)
  }


  override def visitMethodDeclaration(methoddef: MethodDeclaration) {
    processLifeCycleMethod(channelType, methoddef)
  }

  def commonProcess(typeDecl: TypeDeclaration) = {
    processDictionary(channelType, typeDecl)
    processDeployUnit(channelType, typeDecl, env)
    processLibrary(channelType, typeDecl)
    processThirdParty(channelType, typeDecl, env)
    typeDecl.getMethods.foreach {
      method => method.accept(this)
    }
  }

}
