/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kevoree.framework.annotation.processor.visitor.sub

import org.kevoree._
import org.kevoree.framework.annotation.processor.LocalUtility
import javax.lang.model.element.TypeElement
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic.Kind
import org.kevoree.tools.annotation.generator.ThreadingMapping
import scala.collection.JavaConversions._
import scala.Some
import scala.Tuple2
import org.kevoree.annotation.{ProvidedPort, MessageTypes}
import org.kevoree.framework.annotation.processor.visitor.ServicePortTypeVisitor
import scala.collection.JavaConverters._
import scala.Tuple2
import scala.Some


trait ProvidedPortProcessor extends CommonPortProcessor {

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

            val portTypeRef = LocalUtility.kevoreeFactory.createPortTypeRef
            portTypeRef.setName(providedPort.name)

            //sets the reference to the type of the port
            portTypeRef.setRef(LocalUtility.getOraddPortType(definePortType(providedPort, componentType, classdef, env)._1))
            componentType.addProvided(portTypeRef)
          }

          //Two ports have the same name in the component scope, look at the inheritance, otherwise there is a problem due to duplicate port
          case Some(e) => {
            val newPortTypeDefs = definePortType(providedPort, componentType, classdef, env)
            val previousPortType = e.getRef
            val newPortType = newPortTypeDefs._1
            val newSupertypes = newPortTypeDefs._2
            val inheritedType = definedInheritance(newPortType, newSupertypes, previousPortType)
            if (inheritedType.isDefined) {
              if (inheritedType.get == newPortType) {
                env.getMessager.printMessage(Kind.ERROR, "Port (" + providedPort.name + ") in " + componentType.getName + "overrides a port coming from an inherited definition but doesn't respect this inherited definition")
              } else if (inheritedType.get == previousPortType) {
                newSupertypes.foreach {
                  typeDefinition => LocalUtility.getOraddPortType(typeDefinition)
                }

                e.setRef(LocalUtility.getOraddPortType(newPortType))
              } else {
                env.getMessager.printMessage(Kind.ERROR, "Port (" + providedPort.name + ") is duplicated in " + componentType.getName)
              }
            } else {
              env.getMessager.printMessage(Kind.ERROR, "Port (" + providedPort.name + ") is duplicated in " + componentType.getName)
            }
          }
        }

        ThreadingMapping.getMappings.put(Tuple2(componentType.getName, providedPort.name), providedPort.theadStrategy())

    }
  }

  private def definePortType(providedPort: ProvidedPort, componentType: ComponentType, classdef: TypeElement, env: ProcessingEnvironment): (PortType, java.util.List[PortType]) = {
    providedPort.`type` match {

      case org.kevoree.annotation.PortType.SERVICE => {
        //Service port
        val visitor = new ServicePortTypeVisitor(env)
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
        (visitor.getDataType, visitor.getInheritedDataTypes.asScala)
      }

      case org.kevoree.annotation.PortType.MESSAGE => {
        //Message port
        val messagePortType = LocalUtility.kevoreeFactory.createMessagePortType
        messagePortType.setName("org.kevoree.framework.MessagePort")

        if (providedPort.messageType() != "untyped") {
          messagePortType.setName(messagePortType.getName + System.currentTimeMillis()) //ensure uniqueness
        }

        if (providedPort.messageType() != "untyped") {
          if (classdef.getAnnotation(classOf[MessageTypes]) != null) {
            classdef.getAnnotation(classOf[MessageTypes]).value().find(msgType => msgType.name() == providedPort.messageType()) match {
              case Some(foundMessageType) => {
                val dicoType = LocalUtility.kevoreeFactory.createDictionaryType
                foundMessageType.elems().foreach {
                  elem =>
                    val dicAtt = LocalUtility.kevoreeFactory.createDictionaryAttribute
                    dicAtt.setName(elem.name())
                    // messagePortType.setName(messagePortType.getName+elem.name()) //WORKAROUND

                    try {
                      elem.className()
                    } catch {
                      case e: javax.lang.model.`type`.MirroredTypeException =>
                        dicAtt.setDatatype(e.getTypeMirror.toString)
                    }
                    dicAtt.setOptional(elem.optional())
                    dicoType.addAttributes(dicAtt)
                }
                messagePortType.setDictionaryType(dicoType)
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
           val newTypedElement = kevoreeFactory.createTypedElement
           newTypedElement.setName(ndts)
           messagePortType.addFilters(LocalUtility.getOraddDataType(newTypedElement))
       } */
        (messagePortType, List[PortType]())
      }
      case _ => null
    }
  }

  /*private def definedInheritance(newPortType: PortType, inheritedTypes: java.util.List[PortType], previousPortType: PortType): Option[PortType] = {
    if (newPortType.getClass == previousPortType.getClass) {
      newPortType match {
        case portType: ServicePortType =>
          val alreadyExistingType = inheritedTypes.find(inheritedType => {
            inheritedType.modelEquals(previousPortType)
          })
          if (alreadyExistingType.isDefined) {
//            cleanSuperTypes(inheritedTypes, alreadyExistingType.get)
            newPortType.removeSuperTypes(alreadyExistingType.get)
            newPortType.addSuperTypes(previousPortType)
//            inheritedTypes.add(previousPortType)
            Some(previousPortType)
          } else if (isSuperType(newPortType, previousPortType)) {
            Some(newPortType)
          } else {
            None
          }
        case portType: MessagePortType => None
        case _ =>
          None
      }
    } else {
      None
    }
  }

  private def isSuperType(superType: PortType, typeDefinition: TypeDefinition): Boolean = {
    typeDefinition.getSuperTypes.exists(inheritedType => {
      inheritedType.modelEquals(superType)
    } || (inheritedType.isInstanceOf[PortType] && isSuperType(superType, inheritedType.asInstanceOf[PortType])))
  }*/
}
