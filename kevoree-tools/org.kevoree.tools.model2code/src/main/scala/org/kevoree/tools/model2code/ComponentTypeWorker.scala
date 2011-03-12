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
import japa.parser.ast.body.MethodDeclaration
import japa.parser.ast.body.ModifierSet
import japa.parser.ast.body.Parameter
import japa.parser.ast.body.TypeDeclaration
import japa.parser.ast.body.VariableDeclaratorId
import japa.parser.ast.expr.AnnotationExpr
import japa.parser.ast.expr.ArrayInitializerExpr
import japa.parser.ast.expr.BooleanLiteralExpr
import japa.parser.ast.expr.Expression
import japa.parser.ast.expr.FieldAccessExpr
import japa.parser.ast.expr.MarkerAnnotationExpr
import japa.parser.ast.expr.MemberValuePair
import japa.parser.ast.expr.NameExpr
import japa.parser.ast.TypeParameter
import japa.parser.ast.`type`.Type
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.expr.NormalAnnotationExpr
import japa.parser.ast.expr.SingleMemberAnnotationExpr
import japa.parser.ast.expr.StringLiteralExpr
import japa.parser.ast.stmt.BlockStmt
import java.util.ArrayList
import java.util.Collections
import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.kevoree.Operation
import org.kevoree.PortTypeMapping
import org.kevoree.PortTypeRef
import org.kevoree.ServicePortType
import org.kevoree.annotation._
import org.kevoree.framework.MessagePort
import scala.collection.JavaConversions._


class ComponentTypeWorker(componentType : ComponentType, compilationUnit : CompilationUnit) {
  
  def synchronize {
    initCompilationUnit
    syncronizePackage
    var td = sychronizeClass
    synchronizeStart(td)
    synchronizeStop(td)
    synchronizeProvidedPorts(td)
  }
  
  
  private def initCompilationUnit {
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
  
  
  private def syncronizePackage {
    if(compilationUnit.getPackage == null) {
      var packDec = new PackageDeclaration(new NameExpr(componentType.getBean.substring(0,componentType.getBean.lastIndexOf("."))));
      compilationUnit.setPackage(packDec)      
    }  
  }
  
  
  private def sychronizeClass : TypeDeclaration = {
   
    var td = compilationUnit.getTypes.find({typ => typ.getName.equals(componentType.getName)}) match {
      case Some(td:TypeDeclaration) => td
      case None => {
          //Class creation
          var classDecl = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, componentType.getName);
          classDecl.setAnnotations(new ArrayList[AnnotationExpr])
          classDecl.setMembers(new ArrayList[BodyDeclaration])
          ASTHelper.addTypeDeclaration(compilationUnit,classDecl)
          classDecl
        }
    }
    
    checkOrAddMarkerAnnotation(td,classOf[org.kevoree.annotation.ComponentType].getName)
    td
  }
  
  private def synchronizeStart(td : TypeDeclaration) {
    //Check method presence
    var startMethod = new MethodDeclaration
    
    if(componentType.getStartMethod == null || componentType.getStartMethod.equals("")) {
      
      //No start method in the model
      var possibleMethodNames = List("start", "startComponent", "startKevoreeComponent")
      var methods = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]})
      
      //Check available names for start method
      var availableNames = possibleMethodNames.filter({name => 
          methods.filter({method =>
              method.asInstanceOf[MethodDeclaration].getName.equals(name)}).size == 0})
      
      
      startMethod = availableNames.head match {
        case name : String => addDefaultMethod(td, name)
        case _=> {
            printf("No name found for Start method name generation. Please add the start annotation by hand.")
            null
          }
      }
      
    } else {
      startMethod = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
          method.asInstanceOf[MethodDeclaration].getName.equals(componentType.getStartMethod)}) match {
        case Some(m:MethodDeclaration) => m
        case None=> addDefaultMethod(td, componentType.getStartMethod)
      }
    }
    
    //Check annotation
    if(startMethod != null) {
      checkOrAddMarkerAnnotation(startMethod, classOf[Start].getName)
    }
  }
 
  private def synchronizeStop(td : TypeDeclaration) {    
    //Check method presence
    var stopMethod = new MethodDeclaration
    
    if(componentType.getStopMethod == null || componentType.getStopMethod.equals("")) {
      
      //No start method in the model
      var possibleMethodNames = List("stop", "stopComponent", "stopKevoreeComponent")
      var methods = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]})
      
      //Check available names for start method
      var availableNames = possibleMethodNames.filter({name => 
          methods.filter({method =>
              method.asInstanceOf[MethodDeclaration].getName.equals(name)}).size == 0})
      
      
      stopMethod = availableNames.head match {
        case name : String => addDefaultMethod(td, name)
        case _=> {
            printf("No name found for Stop method name generation. Please add the stop annotation by hand.")
            null
          }
      }
      
    } else {
      stopMethod = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
          method.asInstanceOf[MethodDeclaration].getName.equals(componentType.getStopMethod)}) match {
        case Some(m:MethodDeclaration) => m
        case None=> addDefaultMethod(td, componentType.getStopMethod)
      }
    }
    
    //Check annotation
    if(stopMethod != null) {
      checkOrAddMarkerAnnotation(stopMethod, classOf[Stop].getName)
    }
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
  
  private def checkOrAddProvidesAnnotation(td : TypeDeclaration) {
    var annotation : SingleMemberAnnotationExpr = td.getAnnotations.find({annot => 
        annot.getName.toString.equals(classOf[Provides].getSimpleName)}) match {
      case Some(annot : SingleMemberAnnotationExpr) => annot
      case None =>  {
          var annot = createProvidesAnnotation
          td.getAnnotations.add(annot)
          annot
        }
    }
    
    var providedPortAnnotationsList : java.util.List[AnnotationExpr] = if(annotation.getMemberValue == null) {
      new ArrayList[AnnotationExpr]
    } else {
      annotation.getMemberValue match {
        case arrayInitExpr : ArrayInitializerExpr => arrayInitExpr.getValues.asInstanceOf[java.util.List[AnnotationExpr]]
        case _ => new ArrayList[AnnotationExpr]
      }
    }

    componentType.getProvided.foreach { providedPort =>
      printf("Dealing with " + providedPort.getName + " ProvidedPort")
      checkOrAddProvidedPortAnnotation(providedPortAnnotationsList, providedPort, td)
    }
    
    annotation.setMemberValue(new ArrayInitializerExpr(providedPortAnnotationsList.toList))
    
  }
  
  private def checkOrAddProvidedPortAnnotation(annotList : java.util.List[AnnotationExpr], providedPort : PortTypeRef, td : TypeDeclaration) {
    var annotation : NormalAnnotationExpr = annotList.filter({annot => 
        annot.getName.toString.equals(classOf[ProvidedPort].getSimpleName)}).find({annot => 
        annot.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair => 
            pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName)}) match {
      case Some(annot : NormalAnnotationExpr) => annot
      case None =>  {
          var annot = createProvidedPortAnnotation
          annotList.add(annot)
          annot
        }
    }
          
    var pairs = new ArrayList[MemberValuePair]
          
    var portName = new MemberValuePair("name", new StringLiteralExpr(providedPort.getName))
    pairs.add(portName)
    
    
    checkOrAddImport(classOf[PortType].getName)
    providedPort.getRef match {
      case portTypeRef:MessagePortType => {
          var portType = new MemberValuePair("type", new FieldAccessExpr(new NameExpr("PortType"),"MESSAGE"))
          pairs.add(portType)
          
        }
      case portTypeRef:ServicePortType => {
          var portType = new MemberValuePair("type", new FieldAccessExpr(new NameExpr("PortType"),"SERVICE"))
          pairs.add(portType)
          var serviceClass = new MemberValuePair("className", 
                                                 new FieldAccessExpr(
              new NameExpr(portTypeRef.getName.substring(portTypeRef.getName.lastIndexOf(".")+1)),
              "class") )
          pairs.add(serviceClass)
          checkOrAddImport(portTypeRef.getName)
        }        
      case _ =>
    }
    
    annotation.setPairs(pairs)
   
    checkProvidedPortMappedMethod(providedPort, td)
  }
  
  
  private def checkProvidedPortMappedMethod(providedPort : PortTypeRef, td : TypeDeclaration) {
    
    providedPort.getRef match {
      case srvPort : ServicePortType => {
          srvPort.getOperations.foreach{operation =>
            var method : MethodDeclaration = providedPort.getMappings.find({mapping => 
                mapping.getServiceMethodName.equals(operation.getName)}) match {
              case Some(mapping) => {
                  //Check method existence
                  td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
                      method.asInstanceOf[MethodDeclaration].getName.equals(mapping.getBeanMethodName)}) match {
                    case Some(methodDecl) => methodDecl.asInstanceOf[MethodDeclaration]
                    case None => createProvidedServicePortMethod(operation, mapping.getBeanMethodName, td)
                  }
                }
              case None => {
                  //Generate Method
                  var methodName = "on"
                  methodName += operation.getName.substring(0,1).capitalize
                  methodName += operation.getName.substring(1);
                  methodName += "From" + providedPort.getName.substring(0,1).capitalize
                  methodName += providedPort.getName.substring(1) + "PortActivated";
                 
                  //Add Mapping
                  var newMapping = org.kevoree.KevoreeFactory.eINSTANCE.createPortTypeMapping
                  newMapping.setServiceMethodName(operation.getName)
                  newMapping.setBeanMethodName(methodName)
                  providedPort.getMappings.add(newMapping)
                  
                  createProvidedServicePortMethod(operation, methodName, td)
                 
                }
            }
            //Check annotation
            checkOrAddPortAnnotationOnMethod(providedPort, method, operation.getName)
            
          }
        }
      case msgPort : MessagePortType => {
          var method : MethodDeclaration = providedPort.getMappings.find({mapping => mapping.getServiceMethodName.equals("process")}) match {
            case Some(mapping) => {
                //Check method existence
                td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
                    method.asInstanceOf[MethodDeclaration].getName.equals(mapping.getBeanMethodName)}) match {
                  case Some(methodDecl) => methodDecl.asInstanceOf[MethodDeclaration]
                  case None => {
                      createProvidedMessagePortMethod(mapping.getBeanMethodName, td)
                    }
                }
                //Check annotation
              }
            case None => {
                //Generate Method
                var methodName = providedPort.getName.substring(0,1).capitalize
                methodName += providedPort.getName.substring(1);
                 
                //Add Mapping
                var newMapping = org.kevoree.KevoreeFactory.eINSTANCE.createPortTypeMapping
                newMapping.setServiceMethodName("process")
                newMapping.setBeanMethodName(methodName)
                providedPort.getMappings.add(newMapping)
                  
                createProvidedMessagePortMethod(methodName, td)
              }
          }
          //Check annotation
          checkOrAddPortAnnotationOnMethod(providedPort, method, "process")
        }
      case _ =>
    }
    
  }
  
  private def checkOrAddPortAnnotationOnMethod(providedPort : PortTypeRef, method : MethodDeclaration, operationName : String) {
    if(method.getAnnotations == null) {
      method.setAnnotations(new ArrayList[AnnotationExpr])
    }
    
    var mapping = providedPort.getMappings.find({mapping => 
        mapping.getServiceMethodName.equals(operationName)}).head
                  
    var pairs = new ArrayList[MemberValuePair]
          
    var portName = new MemberValuePair("name", new StringLiteralExpr(providedPort.getName))
    pairs.add(portName)
    
    var methName = new MemberValuePair("method", new StringLiteralExpr(operationName))
    pairs.add(methName)
                  
    var newAnnot = new NormalAnnotationExpr(new NameExpr(classOf[Port].getSimpleName), pairs)
          
    method.getAnnotations.size match {
      case 0 => {
          method.getAnnotations.add(newAnnot)
          checkOrAddImport(classOf[Port].getName)
        }
      case 1 => {
          if(method.getAnnotations.head.getName.toString.equals(classOf[Port].getSimpleName)) {
            var portAnnot = method.getAnnotations.head.asInstanceOf[NormalAnnotationExpr]
            if( !portAnnot.getPairs.find({pair => pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName) ||
                !portAnnot.getPairs.find({pair => pair.getName.equals("method")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(operationName) ) {
               var portsAnnot = new SingleMemberAnnotationExpr(new NameExpr(classOf[Ports].getSimpleName), null)
            var memberValue = new ArrayInitializerExpr
            memberValue.setValues(new ArrayList[Expression])
            memberValue.getValues.add(portAnnot)
            memberValue.getValues.add(newAnnot)
            portsAnnot.setMemberValue(memberValue)
            
            method.getAnnotations.remove(portAnnot)
            method.getAnnotations.add(portsAnnot)
            checkOrAddImport(classOf[Ports].getName)
                }
            
            
            
          } else if(method.getAnnotations.head.getName.toString.equals(classOf[Ports].getSimpleName)) {
            method.getAnnotations.head.asInstanceOf[SingleMemberAnnotationExpr].getMemberValue.asInstanceOf[ArrayInitializerExpr].getValues.find({portAnnot =>
              !portAnnot.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair => pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName) ||
              !portAnnot.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair => pair.getName.equals("method")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(operationName)}) match {
               case None => method.getAnnotations.head.asInstanceOf[SingleMemberAnnotationExpr].getMemberValue.asInstanceOf[ArrayInitializerExpr].getValues.add(newAnnot)
               case Some(s) =>
                }
          } else {
            method.getAnnotations.add(newAnnot)
            checkOrAddImport(classOf[Port].getName)
          }
        }
      case _ => {
                  
        }
    }
  }
  
  private def createProvidedServicePortMethod(operation : Operation, methodName : String, td : TypeDeclaration) : MethodDeclaration = {
    var newMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);
    newMethod.setType(new ClassOrInterfaceType(operation.getReturnType.getName))
                        
    //Method body block
    var block = new BlockStmt();
    newMethod.setBody(block);
           
    var parameterList = new ArrayList[Parameter]
                        
    operation.getParameters.foreach{parameter =>
      var param = new Parameter(0, 
                                new ClassOrInterfaceType(parameter.getType.toString),
                                new VariableDeclaratorId(parameter.getName))
      parameterList.add(param)
                        
    }
    newMethod.setParameters(parameterList)
    ASTHelper.addMember(td, newMethod);
    newMethod
  }
  
  private def createProvidedMessagePortMethod(methodName : String, td : TypeDeclaration) : MethodDeclaration = {
    var newMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);
            
    //Method body block
    var block = new BlockStmt();
    newMethod.setBody(block);
           
    var parameterList = new ArrayList[Parameter]
    var param = new Parameter(0, new ClassOrInterfaceType("Object"), new VariableDeclaratorId("msg"))
    parameterList.add(param)
    newMethod.setParameters(parameterList)
           
    ASTHelper.addMember(td, newMethod);
    newMethod
  }
  
  private def createProvidedPortAnnotation : NormalAnnotationExpr = {
    var newAnnot = new NormalAnnotationExpr(new NameExpr(classOf[ProvidedPort].getSimpleName), null)
    checkOrAddImport(classOf[ProvidedPort].getName)
    newAnnot
  }
  
  private def createProvidesAnnotation : SingleMemberAnnotationExpr = {
    var newAnnot = new SingleMemberAnnotationExpr(new NameExpr(classOf[Provides].getSimpleName), null)
    checkOrAddImport(classOf[Provides].getName)
    newAnnot
  }
  
  private def checkOrRemoveAnnotation(declaration : BodyDeclaration, annQName : String) {
    if(declaration.getAnnotations != null) {
      var annSimpleName = annQName.substring(annQName.lastIndexOf(".")+1)
      declaration.getAnnotations.find({ann => ann.getName.toString.equals(annSimpleName)}) match {
        
        case Some(annot : NormalAnnotationExpr) => {
            
            //Remove imports of internal annotations recursively if necessary
            annot.getPairs.foreach{memberPair =>
              memberPair.getValue match {
                case internalAnnot : AnnotationExpr => 
                case _ =>
              }
            }
            
            //Remove annotation
            declaration.getAnnotations.remove(annot)
            
            //Remove Import
            checkOrRemoveImport(annot.getName.toString)
          }
        case Some(annot : SingleMemberAnnotationExpr) => {
            
            //Remove member
            annot.getMemberValue match {
              case annot : AnnotationExpr =>
              case member => printf("AnnotationMember type not foreseen(" + member.getClass.getName + ")")
            }
            
            //Remove annotation
            declaration.getAnnotations.remove(annot)
            
            //Remove Import
            checkOrRemoveImport(annot.getName.toString)
          }
        case Some(annot : MarkerAnnotationExpr) => {
            //Remove annotation
            declaration.getAnnotations.remove(annot)
            
            //Remove Import
            checkOrRemoveImport(annot.getName.toString)
          }
        case None => 
      }
    }
  }
  
  private def checkOrAddMarkerAnnotation(declaration : BodyDeclaration, annQName : String) {
    if(declaration.getAnnotations == null) {
      declaration.setAnnotations(new ArrayList[AnnotationExpr])
    }
    
    var annSimpleName = annQName.substring(annQName.lastIndexOf(".")+1)
    
    declaration.getAnnotations.find({ann => ann.getName.toString.equals(annSimpleName)}) match {
      case None => {
          declaration.getAnnotations.add(new MarkerAnnotationExpr(new NameExpr(annSimpleName)))
        }
      case Some(s)=>
    }
            
    //Adding import declartion
    checkOrAddImport(annQName)
  }
  
  private def addDefaultMethod(td : TypeDeclaration, methodName : String) : MethodDeclaration = {
    //Method declaration
    var method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);
            
    //Method body block
    var block = new BlockStmt();
    method.setBody(block);
           
    //TODO: add a //TODO coment in the empty start method
           
    ASTHelper.addMember(td, method);
            
    componentType.setStartMethod(methodName)
    method
  }

  private def checkOrAddImport(classQName : String) {
    compilationUnit.getImports.find({importDecl => importDecl.getName.toString.equals(classQName)}) match {
      case None => {
          compilationUnit.getImports.add(new ImportDeclaration(new NameExpr(classQName), false, false))
        }
      case Some(s)=>
    }
  }
  
  private def checkOrRemoveImport(classQName : String) {
    compilationUnit.getImports.find({importDecl => importDecl.getName.toString.equals(classQName)}) match {
      case None => //done
      case Some(s)=> {
          //check if class is still used in CU
          //remove if not
        }
    }
  }
}
