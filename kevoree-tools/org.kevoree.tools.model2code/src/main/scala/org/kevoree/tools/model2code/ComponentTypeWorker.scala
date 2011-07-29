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

package org.kevoree.tools.model2code

import japa.parser.ASTHelper
import japa.parser.ast.Comment
import japa.parser.ast.CompilationUnit
import japa.parser.ast.ImportDeclaration
import japa.parser.ast.PackageDeclaration
import japa.parser.ast.body.BodyDeclaration
import japa.parser.ast.body.ClassOrInterfaceDeclaration
import japa.parser.ast.body.ModifierSet
import japa.parser.ast.body.TypeDeclaration
import japa.parser.ast.expr.AnnotationExpr
import japa.parser.ast.expr.NameExpr
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.expr.SingleMemberAnnotationExpr
import java.util.ArrayList
import org.kevoree.ComponentType
import org.kevoree.ContainerRoot
import org.kevoree.PortTypeRef
import org.kevoree.annotation._
import org.kevoree.framework.AbstractComponentType
import scala.collection.JavaConversions._
import sub._

/**
 * @author Gregory NAIN
 */
case class ComponentTypeWorker(root : ContainerRoot, _componentType : org.kevoree.ComponentType, _compilationUnit : CompilationUnit)
  extends LifeCycleSynchMethods
  with ImportSynchMethods
  with DictionarySynchMethods
  with LibrarySynchMethods
  with ProvidedPortSynchMethods
  with RequiredPortSynchMethods
  with AnnotationsSynchMethods {


  def compilationUnit : CompilationUnit = _compilationUnit
  def componentType : ComponentType = _componentType

  def synchronize() {
    initCompilationUnit()
    syncronizePackage()
    val td = sychronizeClass
    synchronizeStart(td)
    synchronizeStop(td)
    synchronizeUpdate(td)
    synchronizeProvidedPorts(td)
    synchronizeRequiredPorts(td)
    synchronizeLibrary(td)
    synchronizeDictionary(td)
  }
  
  //Initiate variables
  private def initCompilationUnit() {
    if(compilationUnit.getComments == null) {
      compilationUnit.setComments(new ArrayList[Comment])
    }
    
    if(compilationUnit.getImports == null) {
      compilationUnit.setImports(new ArrayList[ImportDeclaration])
    }
    
    if(compilationUnit.getTypes == null) {
      compilationUnit.setTypes(new ArrayList[TypeDeclaration])
    }
  }

  /**
   * Synchronize the package name of the compilation unit in the AST
   * If the package does not exist, it takes the value from the BeanName of the Kevoree component
   */
  private def syncronizePackage() {
    if(compilationUnit.getPackage == null) {
      val packDec = new PackageDeclaration(new NameExpr(componentType.getBean.substring(0,componentType.getBean.lastIndexOf("."))));
      compilationUnit.setPackage(packDec)      
    }  
  }

  /**
   * In charge of the class syncronization.
   * Creates and initializes the class, if it does not exist in the compilation unit
   */
  private def sychronizeClass : TypeDeclaration = {
   
    val td = compilationUnit.getTypes.find({typ => typ.getName.equals(componentType.getName)}) match {
      case Some(td:TypeDeclaration) => td
      case None => {
          //Class creation
          val classDecl = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, componentType.getName);
          classDecl.setAnnotations(new ArrayList[AnnotationExpr])
          classDecl.setMembers(new ArrayList[BodyDeclaration])
          ASTHelper.addTypeDeclaration(compilationUnit,classDecl)

          classDecl.setExtends(new ArrayList[ClassOrInterfaceType])
          classDecl.getExtends.add(new ClassOrInterfaceType(classOf[AbstractComponentType].getName))

          checkOrAddImport(classOf[AbstractComponentType].getName)

          classDecl
        }
    }
    
    checkOrAddMarkerAnnotation(td,classOf[org.kevoree.annotation.ComponentType].getName)
    td
  }



  private def synchronizeProvidedPorts(td : TypeDeclaration) {
    
    componentType.getProvided.size match {
      case 0 => {
          //remove provided ports if exists
          checkOrRemoveAnnotation(td, classOf[Provides].getName)
          checkOrRemoveAnnotation(td, classOf[ProvidedPort].getName)
        }
      case 1 => {
          //check ProvidedPort
          checkOrAddProvidedPortAnnotation(td.getAnnotations, componentType.getProvided.head.asInstanceOf[PortTypeRef], td)
        }
      case _ => {
          //check ProvidedPorts
          checkOrAddProvidesAnnotation(td)
        }
    }
  }
  
  private def synchronizeRequiredPorts(td : TypeDeclaration) {
    
    componentType.getRequired.size match {
      case 0 => {
          //remove provided ports if exists
          checkOrRemoveAnnotation(td, classOf[Requires].getName)
          checkOrRemoveAnnotation(td, classOf[RequiredPort].getName)
        }
      case 1 => {
          //check ProvidedPort
          checkOrAddRequiredPortAnnotation(td.getAnnotations, componentType.getRequired.head.asInstanceOf[PortTypeRef], td)
        }
      case _ => {
          //check ProvidedPorts
          checkOrAddRequiresAnnotation(td)
        }
    }
  }

  private def synchronizeLibrary(td : TypeDeclaration) {
    val lib = root.getLibraries.find({libraryType =>
        libraryType.getSubTypes.find({subType => subType.getName.equals(componentType.getName)}) match {
          case Some(s) => true
          case None => false}
      }).head

    //Check Annotation
    checkOrAddLibraryAnnotation(td, lib)
  }

  private def synchronizeDictionary(td : TypeDeclaration) {

    if(componentType.getDictionaryType != null) {
      val dic : SingleMemberAnnotationExpr = td.getAnnotations.find({annot => annot.getName.toString.equals(classOf[DictionaryType].getSimpleName)}) match {
        case Some(annot) => annot.asInstanceOf[SingleMemberAnnotationExpr]
        case None => {
            val newDic = createDictionaryAnnotation()
            td.getAnnotations.add(newDic)
            newDic
          }
      }
      checkOrUpdateDictionary(td, dic)
      // checkOrAddImport(compilationUnit, classOf[Library].getName)
    }

  }


}
