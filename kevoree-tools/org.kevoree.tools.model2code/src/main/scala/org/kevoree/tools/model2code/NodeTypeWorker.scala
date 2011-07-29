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
import org.kevoree.ContainerRoot
import org.kevoree.NodeType
import scala.collection.JavaConversions._
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.body._
import japa.parser.ast.stmt.BlockStmt
import org.kevoree.framework.AbstractNodeType
import org.kevoreeAdaptation.AdaptationModel

/**
 * @author Gregory NAIN
 */
case class NodeTypeWorker(root: ContainerRoot, _nodeType: NodeType, _compilationUnit: CompilationUnit)
  extends ImportSynchMethods
  with AnnotationsSynchMethods
  with LifeCycleSynchMethods
  with LibrarySynchMethods
  with DictionarySynchMethods {


  def compilationUnit: CompilationUnit = _compilationUnit

  def nodeType: NodeType = _nodeType

  def synchronize() {
    initCompilationUnit()
    syncronizePackage()
    val td = sychronizeClass
    synchronizeStart(td, "startNode")
    synchronizeStop(td, "stopNode")
    synchronizeLibrary(root, td, nodeType.getName)
    synchronizeDictionary(td, nodeType)
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
      val packDec = new PackageDeclaration(new NameExpr(nodeType.getBean.substring(0, nodeType.getBean.lastIndexOf("."))));
      compilationUnit.setPackage(packDec)
    }
  }

  /**
   * In charge of the class syncronization.
   * Creates and initializes the class, if it does not exist in the compilation unit
   */
  private def sychronizeClass: TypeDeclaration = {

    val td = compilationUnit.getTypes.find({
      typ => typ.getName.equals(nodeType.getName)
    }) match {
      case Some(td: TypeDeclaration) => {
        System.out.print("Synchronizing " + nodeType.getName + "...")
        td
      }
      case None => {
        System.out.print("Generating " + nodeType.getName + "...")
        //Class creation
        val classDecl = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, nodeType.getName);
        classDecl.setAnnotations(new ArrayList[AnnotationExpr])
        classDecl.setMembers(new ArrayList[BodyDeclaration])
        ASTHelper.addTypeDeclaration(compilationUnit, classDecl)

        classDecl.setExtends(new ArrayList[ClassOrInterfaceType])
        classDecl.getExtends.add(new ClassOrInterfaceType(classOf[AbstractNodeType].getSimpleName))

        checkOrAddImport(classOf[AbstractNodeType].getName)

        classDecl
      }
    }

    checkOrAddMarkerAnnotation(td, classOf[NodeType].getName)
    td
  }

  private def synchronizeMandatoryMethods(td: TypeDeclaration) {
    val methods = td.getMembers.filter({
      member => member.isInstanceOf[MethodDeclaration]
    })
    methods.find({
      method => method.asInstanceOf[MethodDeclaration].getName.equals("push")
    }) match {
      case None => {
        createPushMethod(td)
      }
      case Some(m) => //ok
    }

    /*
    //TODO: Check if useful
    methods.find({
      method => method.asInstanceOf[MethodDeclaration].getName.equals("deploy")
    }) match {
      case None => {
        createDeployMethod(td)
      }
      case Some(m) => //ok
    }
    */
  }

  private def createPushMethod(td: TypeDeclaration) {
    val dispatch = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "push");
    dispatch.setModifiers(ModifierSet.PUBLIC)
    dispatch.setParameters(new ArrayList[Parameter])
    dispatch.getParameters.add(new Parameter(0,
      new ClassOrInterfaceType("String"),
      new VariableDeclaratorId("targetNodeName")))
    dispatch.getParameters.add(new Parameter(0,
      new ClassOrInterfaceType(classOf[ContainerRoot].getSimpleName),
      new VariableDeclaratorId("root")))
    dispatch.getParameters.add(new Parameter(0,
      new ClassOrInterfaceType("BundleContext"),
      new VariableDeclaratorId("context")))

    //Method body block
    val block = new BlockStmt();
    dispatch.setBody(block);

    checkOrAddImport(classOf[ContainerRoot].getName)
    checkOrAddImport("org.osgi.framework.BundleContext")
    ASTHelper.addMember(td, dispatch);
  }

  //TODO: Check if useful
  /*
  private def createDeployMethod(td: TypeDeclaration) {
    val dispatch = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.BOOLEAN_TYPE, "deploy");
    dispatch.setModifiers(ModifierSet.PUBLIC)
    dispatch.setParameters(new ArrayList[Parameter])
    dispatch.getParameters.add(new Parameter(0,
      new ClassOrInterfaceType(classOf[AdaptationModel].getSimpleName),
      new VariableDeclaratorId("model")))
    dispatch.getParameters.add(new Parameter(0,
      new ClassOrInterfaceType("String"),
      new VariableDeclaratorId("nodeName")))
    //Method body block
    val block = new BlockStmt();
    dispatch.setBody(block);

    checkOrAddImport(classOf[AdaptationModel].getName)
    ASTHelper.addMember(td, dispatch);
  }
  */

}
