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

package org.kevoree.framework.annotation.processor.visitor.sub

import com.sun.mirror.apt.AnnotationProcessorEnvironment
import com.sun.mirror.declaration.TypeDeclaration
import org.kevoree.ComponentType
import org.kevoree.KevoreeFactory
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.visitor.ServicePortTypeVisitor
import scala.collection.JavaConversions._
import com.sun.mirror.`type`.{InterfaceType, ClassType}

trait RequiredPortProcessor {
  def processRequiredPort(componentType: ComponentType, classdef: TypeDeclaration, env: AnnotationProcessorEnvironment) = {

    //Collects all RequidedPort annotations and creates a list
    var requiredPortAnnotations: List[org.kevoree.annotation.RequiredPort] = Nil

    val annotationRequired = classdef.getAnnotation(classOf[org.kevoree.annotation.RequiredPort])
    if (annotationRequired != null) {
      requiredPortAnnotations = requiredPortAnnotations ++ List(annotationRequired)
    }

    val annotationRequires = classdef.getAnnotation(classOf[org.kevoree.annotation.Requires])
    if (annotationRequires != null) {
      requiredPortAnnotations = requiredPortAnnotations ++ annotationRequires.value.toList
    }

    //For each annotation in the list
    requiredPortAnnotations.foreach {
      requiredPort =>


      //Check if a port with the same name exist in the component scope
        val portAll: List[org.kevoree.PortTypeRef] = componentType.getRequired.toList ++ componentType.getProvided.toList
        portAll.find(existingPort => existingPort.getName == requiredPort.name) match {

        case None => {
            val portTypeRef = KevoreeFactory.eINSTANCE.createPortTypeRef
            portTypeRef.setName(requiredPort.name)
            portTypeRef.setOptional(requiredPort.optional)
          /*
          we replace the annotation parameter "noDependency" by "needCheckDependency but we do not replace noDependency on the model
          noDependency = true (equivalent to needCheckDependency = false) means the required port is not use during critical start and stop operations of the container component
          and so we do not need to take it into account when we try to schedule starts and stops to avoid some deadlocks.
          noDependency = false (equivalent to needCheckDependency = true) means that the required port is used on start or stop operations of the container component
          and so we need to check dependency to avoid some deadlocks
           */
          portTypeRef.setNoDependency(!requiredPort.needCheckDependency())

            //sets the reference to the type of the port
            portTypeRef.setRef(LocalUtility.getOraddPortType(requiredPort.`type` match {
              case org.kevoree.annotation.PortType.SERVICE => {
                val visitor = new ServicePortTypeVisitor
                try {
                  requiredPort.className
                } catch {
                  case e: com.sun.mirror.`type`.MirroredTypeException =>

                    //Checks the kind of the className attribute of the annotation
                    e.getTypeMirror match {
                      case mirrorType: com.sun.tools.apt.mirror.`type`.ClassTypeImpl => mirrorType.accept(visitor)
                      case mirrorType: com.sun.tools.apt.mirror.`type`.InterfaceTypeImpl => mirrorType.accept(visitor)
                      case _ @ e => {
                        println(e)
                        env.getMessager.printError("The className attribute of a Required ServicePort declaration is mandatory, and must be a Class or an Interface.\n"
                          + "Have a check on RequiredPort[name=" + requiredPort.name + "] of " + componentType.getBean)
                      }
                    }

                }
                visitor.getDataType
              }
              case org.kevoree.annotation.PortType.MESSAGE => {
                val mpt = KevoreeFactory.eINSTANCE.createMessagePortType
                mpt.setName("org.kevoree.framework.MessagePort")
                requiredPort.filter.foreach {
                  ndts =>
                    val ndt = KevoreeFactory.eINSTANCE.createTypedElement
                    ndt.setName(ndts)
                    mpt.getFilters.add(LocalUtility.getOraddDataType(ndt))
                }
                mpt
              }
              case _ => null
            }))
            componentType.getRequired.add(portTypeRef)
          }
          case Some(e) => {
            env.getMessager.printError("Port name duplicated in " + componentType.getName + " Scope => " + requiredPort.name)
          }
        }
    }
  }
}
