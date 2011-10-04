package org.kevoree.tools.model2code

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

import genericSub._
import japa.parser.ASTHelper
import japa.parser.ast.Comment
import japa.parser.ast.CompilationUnit
import japa.parser.ast.ImportDeclaration
import japa.parser.ast.PackageDeclaration
import japa.parser.ast.expr.AnnotationExpr
import japa.parser.ast.expr.NameExpr
import java.util.ArrayList
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.body._
import japa.parser.ast.stmt.BlockStmt
import org.kevoree.{ContainerRoot, ChannelType}
import org.kevoree.framework.message.Message

import scala.collection.JavaConversions._
import org.kevoree.framework.{ChannelFragmentSender, AbstractChannelFragment}

/**
 * @author Gregory NAIN
 */
case class ChannelTypeWorker(root: ContainerRoot, _channelType: ChannelType, _compilationUnit: CompilationUnit)
  extends ImportSynchMethods
  with AnnotationsSynchMethods
  with LifeCycleSynchMethods
  with LibrarySynchMethods
  with DictionarySynchMethods {


  def compilationUnit: CompilationUnit = _compilationUnit

  def channelType : ChannelType = _channelType

  def synchronize() {
    initCompilationUnit()
    syncronizePackage()
    val td = sychronizeClass
    synchronizeStart(td, channelType.getStartMethod)
    synchronizeStop(td, channelType.getStopMethod)
    synchronizeUpdate(td, channelType.getUpdateMethod)
    synchronizeLibrary(root, td, channelType.getName)
    synchronizeDictionary(td, channelType)
    synchronizeMandatoryMethods(td)
    System.out.println("Done.")
  }

  //Initiate variables
  private def initCompilationUnit() {
    if (compilationUnit.getComments == null) {
      compilationUnit.setComments(new ArrayList[Comment])
    }

    if (compilationUnit.getImports == null) {
      compilationUnit.setImports(new ArrayList[ImportDeclaration])
    }

    if (compilationUnit.getTypes == null) {
      compilationUnit.setTypes(new ArrayList[TypeDeclaration])
    }
  }

  /**
   * Synchronize the package name of the compilation unit in the AST
   * If the package does not exist, it takes the value from the BeanName of the Kevoree component
   */
  private def syncronizePackage() {
    if (compilationUnit.getPackage == null) {
      val packDec = new PackageDeclaration(new NameExpr(channelType.getBean.substring(0, channelType.getBean.lastIndexOf("."))));
      compilationUnit.setPackage(packDec)
    }
  }

  /**
   * In charge of the class syncronization.
   * Creates and initializes the class, if it does not exist in the compilation unit
   */
  private def sychronizeClass : TypeDeclaration = {

    val td = compilationUnit.getTypes.find({
      typ => typ.getName.equals(channelType.getName)
    }) match {
      case Some(td: TypeDeclaration) => {
        System.out.print("Synchronizing " + channelType.getName + "...")
        td
      }
      case None => {
        System.out.print("Generating " + channelType.getName + "...")
        //Class creation
        val classDecl = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, channelType.getName);
        classDecl.setAnnotations(new ArrayList[AnnotationExpr])
        classDecl.setMembers(new ArrayList[BodyDeclaration])
        ASTHelper.addTypeDeclaration(compilationUnit, classDecl)

        classDecl.setExtends(new ArrayList[ClassOrInterfaceType])
        classDecl.getExtends.add(new ClassOrInterfaceType(classOf[AbstractChannelFragment].getSimpleName))

        checkOrAddImport(classOf[AbstractChannelFragment].getName)

        classDecl
      }
    }

    checkOrAddMarkerAnnotation(td, classOf[org.kevoree.annotation.ChannelTypeFragment].getName)
    td
  }

   private def synchronizeMandatoryMethods(td : TypeDeclaration) {
    val methods = td.getMembers.filter({member=>member.isInstanceOf[MethodDeclaration]})
     methods.find({method=>method.asInstanceOf[MethodDeclaration].getName.equals("dispatch")}) match {
       case None => {
         createDispatchMethod(td)
       }
       case Some(m) => //ok
     }

     methods.find({method=>method.asInstanceOf[MethodDeclaration].getName.equals("createSender")}) match {
       case None => {
         createCreateSenderMethod(td)
       }
       case Some(m) => //ok
     }
   }

  private def createDispatchMethod(td : TypeDeclaration) {
    val dispatch = new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType("Object"), "dispatch");
         dispatch.setModifiers(ModifierSet.PUBLIC)
         dispatch.setParameters(new ArrayList[Parameter])
         dispatch.getParameters.add(new Parameter(0,
                                new ClassOrInterfaceType(classOf[Message].getSimpleName),
                                new VariableDeclaratorId("msg")))

             //Method body block
            val block = new BlockStmt();
            dispatch.setBody(block);

         checkOrAddImport(classOf[Message].getName)
         ASTHelper.addMember(td, dispatch);
  }

  private def createCreateSenderMethod(td : TypeDeclaration) {
    val dispatch = new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType(classOf[ChannelFragmentSender].getSimpleName), "createSender");
         dispatch.setModifiers(ModifierSet.PUBLIC)
         dispatch.setParameters(new ArrayList[Parameter])
         dispatch.getParameters.add(new Parameter(0,
                                new ClassOrInterfaceType("String"),
                                new VariableDeclaratorId("remoteNodeName")))
        dispatch.getParameters.add(new Parameter(0,
                                new ClassOrInterfaceType("String"),
                                new VariableDeclaratorId("remoteChannelName")))
             //Method body block
            val block = new BlockStmt();
            dispatch.setBody(block);

         checkOrAddImport(classOf[ChannelFragmentSender].getName)
         ASTHelper.addMember(td, dispatch);
  }

}
