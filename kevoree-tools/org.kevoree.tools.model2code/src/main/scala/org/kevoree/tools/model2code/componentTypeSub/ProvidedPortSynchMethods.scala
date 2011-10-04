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
package org.kevoree.tools.model2code.componentTypeSub


import java.util.ArrayList
import japa.parser.ast.expr._
import japa.parser.ast.CompilationUnit
import org.kevoree.annotation._
import org.kevoree.{ Operation, ServicePortType, MessagePortType, PortTypeRef}
import japa.parser.ASTHelper
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.stmt.BlockStmt
import japa.parser.ast.body._
import org.kevoree.tools.model2code.genericSub.ImportSynchMethods

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 10:49
 */

trait ProvidedPortSynchMethods
  extends ImportSynchMethods {

  def compilationUnit : CompilationUnit
  def componentType : org.kevoree.ComponentType

  def checkOrAddProvidedPortAnnotation(annotList : java.util.List[AnnotationExpr], providedPort : PortTypeRef, td : TypeDeclaration) {
    val annotation : NormalAnnotationExpr = annotList.filter({annot =>
        annot.getName.toString.equals(classOf[ProvidedPort].getSimpleName)}).find({annot =>
        annot.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair =>
            pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName)}) match {
      case Some(annot : NormalAnnotationExpr) => annot
      case None =>  {
          val annot = createProvidedPortAnnotation
          annotList.add(annot)
          annot
        }
    }

    val pairs = new ArrayList[MemberValuePair]

    val portName = new MemberValuePair("name", new StringLiteralExpr(providedPort.getName))
    pairs.add(portName)


    checkOrAddImport(classOf[PortType].getName)
    providedPort.getRef match {
      case portTypeRef:MessagePortType => {
          val portType = new MemberValuePair("type", new FieldAccessExpr(new NameExpr("PortType"),"MESSAGE"))
          pairs.add(portType)

        }
      case portTypeRef:ServicePortType => {
          val portType = new MemberValuePair("type", new FieldAccessExpr(new NameExpr("PortType"),"SERVICE"))
          pairs.add(portType)
          val serviceClass = new MemberValuePair("className",
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

  def checkProvidedPortMappedMethod(providedPort : PortTypeRef, td : TypeDeclaration) {

    providedPort.getRef match {
      case srvPort : ServicePortType => {

          //For each operation of the service port
          srvPort.getOperations.foreach{operation =>

            //Search if a mapping is already present in the model for this operation
            val method : MethodDeclaration = providedPort.getMappings.find({mapping =>
                mapping.getServiceMethodName.equals(operation.getName)}) match {

              //Mapping present
              case Some(mapping) => {

                  //Check method existence
                  td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
                      method.asInstanceOf[MethodDeclaration].getName.equals(mapping.getBeanMethodName)}) match {
                    //Method Exists : return method
                    case Some(methodDecl) => methodDecl.asInstanceOf[MethodDeclaration]
                      //Method not found, create Method using the method name present in mapping declaration
                    case None => createProvidedServicePortMethod(operation, mapping.getBeanMethodName, td)
                  }
                }

                //No Mapping
              case None => {
                  //Find a method name not already used
                  var methodName = "on"
                  methodName += operation.getName.substring(0,1).capitalize
                  methodName += operation.getName.substring(1);
                  methodName += "From" + providedPort.getName.substring(0,1).capitalize
                  methodName += providedPort.getName.substring(1) + "PortActivated";

                  //Add Mapping
                  val newMapping = org.kevoree.KevoreeFactory.eINSTANCE.createPortTypeMapping
                  newMapping.setServiceMethodName(operation.getName)
                  newMapping.setBeanMethodName(methodName)
                  providedPort.getMappings.add(newMapping)

                  //Create the method
                  createProvidedServicePortMethod(operation, methodName, td)

                }
            }
            //Check annotation on the method corresponding to the operation
            checkOrAddPortAnnotationOnMethod(providedPort, method, operation.getName)
          }
        }
      case msgPort : MessagePortType => {
          //Search if a mapping is already present in the model for this operation
          val method : MethodDeclaration = providedPort.getMappings.find({mapping => mapping.getServiceMethodName.equals("process")}) match {
            //Mapping present
            case Some(mapping) => {
                //Check method existence
                td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
                    method.asInstanceOf[MethodDeclaration].getName.equals(mapping.getBeanMethodName)}) match {
                  //Method Exists : return method
                  case Some(methodDecl) => methodDecl.asInstanceOf[MethodDeclaration]
                    //Method not found, create Method using the method name present in mapping declaration
                  case None => {
                      createProvidedMessagePortMethod(mapping.getBeanMethodName, td)
                    }
                }
              }
              //No Mapping
            case None => {

                //Find a method name not already used
                var methodName = providedPort.getName.substring(0,1).capitalize
                methodName += providedPort.getName.substring(1);

                //Add Mapping
                val newMapping = org.kevoree.KevoreeFactory.eINSTANCE.createPortTypeMapping
                newMapping.setServiceMethodName("process")
                newMapping.setBeanMethodName(methodName)
                providedPort.getMappings.add(newMapping)

                //Create Method
                createProvidedMessagePortMethod(methodName, td)
              }
          }
          //Check annotation
          checkOrAddPortAnnotationOnMethod(providedPort, method, "process")
        }
      case _ =>
    }

  }


  def checkOrAddPortAnnotationOnMethod(providedPort : PortTypeRef, method : MethodDeclaration, operationName : String) {

    //If method newly created, annotation list is null
    if(method.getAnnotations == null) {
      method.setAnnotations(new ArrayList[AnnotationExpr])
    }

    //retreive concerned mapping
    providedPort.getMappings.find({mapping =>
        mapping.getServiceMethodName.equals(operationName)}).head

    //creates the new annotation
    val newAnnot = createPortAnnotation(providedPort, operationName)

    val usefullAnnots = method.getAnnotations.filter({annot => (annot.getName.toString.equals(classOf[Port].getSimpleName)||annot.getName.toString.equals(classOf[Ports].getSimpleName))})

    usefullAnnots.size match {
      case 0 => {
          method.getAnnotations.add(newAnnot)
          checkOrAddImport(classOf[Port].getName)
        }
      case 1 => {
          if(usefullAnnots.head.getName.toString.equals(classOf[Port].getSimpleName)) {
            checkOrAddPortMapping(method, usefullAnnots.head.asInstanceOf[NormalAnnotationExpr], newAnnot, providedPort, operationName)
          } else if(usefullAnnots.head.getName.toString.equals(classOf[Ports].getSimpleName)) {
            checkOrAddPortMappingAnnotationToPortsAnnotation(usefullAnnots.head.asInstanceOf[SingleMemberAnnotationExpr], newAnnot, providedPort, operationName)
          } else {
            method.getAnnotations.add(newAnnot)
            checkOrAddImport(classOf[Port].getName)
          }
        }
      case _ => { //NOT POSSIBLE
          printf("ERROR")
          /*
           method.getAnnotations.find({annot => annot.asInstanceOf[NormalAnnotationExpr].getName.toString.equals(classOf[Ports].getSimpleName)}) match {
           case Some(portsAnnot : SingleMemberAnnotationExpr) => {
           checkOrAddPortMappingAnnotationToPortsAnnotation(portsAnnot, newAnnot, providedPort, operationName)
           }
           case None => {
           method.getAnnotations.find({annot => annot.asInstanceOf[NormalAnnotationExpr].getName.toString.equals(classOf[Port].getSimpleName)}) match {
           case Some(annot : NormalAnnotationExpr) => {
           changeAndAdd_PortToPorts(method, method.getAnnotations.head.asInstanceOf[NormalAnnotationExpr], newAnnot, providedPort, operationName)
           }
           case None => {
           method.getAnnotations.add(newAnnot)
           checkOrAddImport(compilationUnit, classOf[Port].getName)
           }
           }
           }
           }*/
        }
    }
  }

  def createPortAnnotation(providedPort : PortTypeRef, operationName : String) = {
    val pairs = new ArrayList[MemberValuePair]

    val portName = new MemberValuePair("name", new StringLiteralExpr(providedPort.getName))
    pairs.add(portName)

    val methName = new MemberValuePair("method", new StringLiteralExpr(operationName))
    pairs.add(methName)

    new NormalAnnotationExpr(new NameExpr(classOf[Port].getSimpleName), pairs)
  }

  def checkOrAddPortMapping(method : MethodDeclaration, portAnnot : NormalAnnotationExpr, newAnnot : NormalAnnotationExpr, providedPort : PortTypeRef, operationName : String) {
    val portAnnot = method.getAnnotations.head.asInstanceOf[NormalAnnotationExpr]

    providedPort.getRef match {
      case srv : ServicePortType =>  {
          if( !portAnnot.getPairs.find({pair => pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName) ||
             !portAnnot.getPairs.find({pair => pair.getName.equals("method")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(operationName) ) {
            changeAndAdd_PortToPorts(method, portAnnot, newAnnot)
          }
        }
      case msg : MessagePortType => {
          if( !portAnnot.getPairs.find({pair => pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName) ) {
            changeAndAdd_PortToPorts(method, portAnnot, newAnnot)
          }
        }
      case _ =>
    }
  }

  def changeAndAdd_PortToPorts(method : MethodDeclaration, portAnnot : NormalAnnotationExpr, newAnnot : NormalAnnotationExpr) {

    val portsAnnot = new SingleMemberAnnotationExpr(new NameExpr(classOf[Ports].getSimpleName), null)
    val memberValue = new ArrayInitializerExpr
    memberValue.setValues(new ArrayList[Expression])
    memberValue.getValues.add(portAnnot)
    memberValue.getValues.add(newAnnot)
    portsAnnot.setMemberValue(memberValue)

    method.getAnnotations.remove(portAnnot)
    method.getAnnotations.add(portsAnnot)
    checkOrAddImport(classOf[Ports].getName)

  }

  def checkOrAddPortMappingAnnotationToPortsAnnotation(portsAnnotation : SingleMemberAnnotationExpr, newAnnot : NormalAnnotationExpr, providedPort : PortTypeRef, operationName : String) {
    portsAnnotation.getMemberValue.asInstanceOf[ArrayInitializerExpr].getValues.find({portAnnot =>
        !portAnnot.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair => pair.getName.equals("name")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(providedPort.getName) ||
        !portAnnot.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair => pair.getName.equals("method")}).head.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(operationName)}) match {
      case None => portsAnnotation.getMemberValue.asInstanceOf[ArrayInitializerExpr].getValues.add(newAnnot)
      case Some(s) =>
    }
  }


  def checkOrAddProvidesAnnotation(td : TypeDeclaration) {
    val annotation : SingleMemberAnnotationExpr = td.getAnnotations.find({annot =>
        annot.getName.toString.equals(classOf[Provides].getSimpleName)}) match {
      case Some(annot : SingleMemberAnnotationExpr) => annot
      case None =>  {
          val annot = createProvidesAnnotation
          td.getAnnotations.add(annot)
          annot
        }
    }

    val providedPortAnnotationsList : java.util.List[AnnotationExpr] = if(annotation.getMemberValue == null) {
      new ArrayList[AnnotationExpr]
    } else {
      annotation.getMemberValue match {
        case arrayInitExpr : ArrayInitializerExpr => arrayInitExpr.getValues.asInstanceOf[java.util.List[AnnotationExpr]]
        case _ => new ArrayList[AnnotationExpr]
      }
    }

    componentType.getProvided.foreach { providedPort =>
      
      checkOrAddProvidedPortAnnotation(providedPortAnnotationsList, providedPort, td)
    }

    annotation.setMemberValue(new ArrayInitializerExpr(providedPortAnnotationsList.toList))

  }

  def createProvidedPortAnnotation : NormalAnnotationExpr = {
    val newAnnot = new NormalAnnotationExpr(new NameExpr(classOf[ProvidedPort].getSimpleName), null)
    checkOrAddImport(classOf[ProvidedPort].getName)
    newAnnot
  }


  def createProvidesAnnotation : SingleMemberAnnotationExpr = {
    val newAnnot = new SingleMemberAnnotationExpr(new NameExpr(classOf[Provides].getSimpleName), null)
    checkOrAddImport(classOf[Provides].getName)
    newAnnot
  }

  def createProvidedServicePortMethod(operation : Operation, methodName : String, td : TypeDeclaration) : MethodDeclaration = {
    val newMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);
    newMethod.setType(new ClassOrInterfaceType(operation.getReturnType.getName))

    //Method body block
    val block = new BlockStmt();
    newMethod.setBody(block);

    val parameterList = new ArrayList[Parameter]

    operation.getParameters.foreach{parameter =>
      val param = new Parameter(0,
                                new ClassOrInterfaceType(parameter.getType.toString),
                                new VariableDeclaratorId(parameter.getName))
      parameterList.add(param)

    }
    newMethod.setParameters(parameterList)
    ASTHelper.addMember(td, newMethod);
    newMethod
  }

  def createProvidedMessagePortMethod(methodName : String, td : TypeDeclaration) : MethodDeclaration = {
    val newMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);

    //Method body block
    val block = new BlockStmt();
    newMethod.setBody(block);

    val parameterList = new ArrayList[Parameter]
    val param = new Parameter(0, new ClassOrInterfaceType("Object"), new VariableDeclaratorId("msg"))
    parameterList.add(param)
    newMethod.setParameters(parameterList)

    ASTHelper.addMember(td, newMethod);
    newMethod
  }

}