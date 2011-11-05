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

import org.kevoree.KevoreeFactory
import org.kevoree.ComponentType
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.visitor.ServicePortTypeVisitor
import javax.lang.model.element.TypeElement
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic.Kind
import org.kevoree.annotation.MessageTypes


trait ProvidedPortProcessor {

  def processProvidedPort(componentType: ComponentType, classdef: TypeElement, env: ProcessingEnvironment) = {

    //Collects all ProvidedPort annotations and creates a list
    var providedPortAnnotations: List[org.kevoree.annotation.ProvidedPort] = Nil

    val annotationProvided = classdef.getAnnotation(classOf[org.kevoree.annotation.ProvidedPort])
    if (annotationProvided != null) {
      providedPortAnnotations = providedPortAnnotations ++ List(annotationProvided)
    }

    val annotationProvides = classdef.getAnnotation(classOf[org.kevoree.annotation.Provides])
    if (annotationProvides != null) {
      providedPortAnnotations = providedPortAnnotations ++ annotationProvides.value.toList
    }

    //For each annotation in the list
    providedPortAnnotations.foreach {
      providedPort =>

      //Check if a port with the same name exist in the component scope
        val allComponentPorts: List[org.kevoree.PortTypeRef] = componentType.getRequired.toList ++ componentType.getProvided.toList
        allComponentPorts.find(existingPort => existingPort.getName == providedPort.name) match {

          //Port is unique and can be created
          case None => {

            val portTypeRef = KevoreeFactory.eINSTANCE.createPortTypeRef
            portTypeRef.setName(providedPort.name)

            //sets the reference to the type of the port
            portTypeRef.setRef(LocalUtility.getOraddPortType(providedPort.`type` match {

              case org.kevoree.annotation.PortType.SERVICE => {
                //Service port
                val visitor = new ServicePortTypeVisitor
                try {
                  providedPort.className
                } catch {
                  case e: javax.lang.model.`type`.MirroredTypeException =>

                    //Checks the kind of the className attribute of the annotation
                    if (!e.getTypeMirror.toString.equals("java.lang.Void")) {
                      e.getTypeMirror.accept(visitor, e.getTypeMirror)
                    } else {
                      env.getMessager.printMessage(Kind.ERROR, "The className attribute of a Provided ServicePort declaration is mandatory, and must be a Class or an Interface.\n"
                        + "Have a check on ProvidedPort[name=" + providedPort.name + "] of " + componentType.getBean + "\n"
                        + "TypeMirror of " + providedPort.name + ", typeMirror : " + e.getTypeMirror + ",  qualifiedName : " + e.getTypeMirror + ", typeMirrorClass : " + e.getTypeMirror.getClass + "\n")
                    }

                }

                visitor.getDataType
              }

              case org.kevoree.annotation.PortType.MESSAGE => {
                //Message port
                val messagePortType = KevoreeFactory.eINSTANCE.createMessagePortType
                messagePortType.setName("org.kevoree.framework.MessagePort")

                if (providedPort.messageType() != "untyped") {
                  if (classdef.getAnnotation(classOf[MessageTypes]) != null) {
                    classdef.getAnnotation(classOf[MessageTypes]).value().find(msgType => msgType.name() == providedPort.messageType()) match {
                      case Some(foundMessageType) => {
                        val dicoType = KevoreeFactory.eINSTANCE.createDictionaryType
                        foundMessageType.elems().foreach {
                          elem =>
                            val dicAtt = KevoreeFactory.eINSTANCE.createDictionaryAttribute
                            dicAtt.setName(elem.name())
                            try {
                              elem.className()
                            } catch {
                              case e: javax.lang.model.`type`.MirroredTypeException =>
                                dicAtt.setDatatype(e.getTypeMirror.toString)
                            }
                            dicoType.addAttributes(dicAtt)
                        }
                        messagePortType.setDictionaryType(Some(dicoType))
                      }
                      case None => env.getMessager.printMessage(Kind.ERROR, "Can't find message type for name " + providedPort.messageType() + " for port " + providedPort.name())
                    }
                  } else {
                    env.getMessager.printMessage(Kind.ERROR, "Can't find message type for name " + providedPort.messageType() + " for port " + providedPort.name())
                  }
                }

                /*
               providedPort.filter.foreach {
                 ndts =>
                   val newTypedElement = KevoreeFactory.eINSTANCE.createTypedElement
                   newTypedElement.setName(ndts)
                   messagePortType.addFilters(LocalUtility.getOraddDataType(newTypedElement))
               } */
                messagePortType
              }
              case _ => null
            }))
            componentType.addProvided(portTypeRef)
          }

          //Two ports have the same name in the component scope
          case Some(e) => {
            env.getMessager.printMessage(Kind.ERROR, "Port name duplicated in " + componentType.getName + " Scope => " + providedPort.name)
          }
        }

    }
  }

}
