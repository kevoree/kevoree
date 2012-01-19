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
import org.kevoree.MessagePortType
import org.kevoree.ServicePortType
import scala.collection.JavaConversions._
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.tools.Diagnostic.Kind


trait PortMappingProcessor {

  var starMethod: String = ""

  def doAnnotationPostProcess(componentType: ComponentType) {
    if (starMethod != "") {
      componentType.getProvided.foreach {
        pref =>
          if (pref.getMappings.size == 0 && pref.getRef.isInstanceOf[MessagePortType]) {
            val ptREFmapping = KevoreeFactory.eINSTANCE.createPortTypeMapping
            ptREFmapping.setBeanMethodName(starMethod)
            ptREFmapping.setServiceMethodName("process")
            pref.addMappings(ptREFmapping)
          }
      }
      starMethod = ""
    }
  }


  def processPortMapping(componentType: ComponentType, methoddef: ExecutableElement, env: ProcessingEnvironment) = {
    /* PROCESS PORTS & PORT ANNOTATION */
    var portAnnotations: List[org.kevoree.annotation.Port] = List()

    val annotationPort = methoddef.getAnnotation(classOf[org.kevoree.annotation.Port])
    if (annotationPort != null) {
      portAnnotations = portAnnotations ++ List(annotationPort)
    }

    val annotationPorts = methoddef.getAnnotation(classOf[org.kevoree.annotation.Ports])
    if (annotationPorts != null) {
      portAnnotations = portAnnotations ++ annotationPorts.value.toList
    }

    if (portAnnotations.size > 0 && methoddef.getModifiers.find {
      mod => mod.name.equals("PUBLIC")
    }.isEmpty) {
      env.getMessager.printMessage(Kind.ERROR, "Method " + methoddef.getSimpleName + " in " + componentType.getName + " must have a public visibility since it is mapped to a port.")
    }

    portAnnotations.foreach {
      annot =>

        annot.name match {
          case "*" => {
            if (starMethod == "") {
              starMethod = methoddef.getSimpleName.toString
            } else {
              val message: String = "[PortMappingProcessor]:" + componentType.getBean + " declares a severals * mapping, but only one * is accepted. Process Exit.";
              env.getMessager.printMessage(Kind.ERROR, message);
            }
          }
          case _@genName => {
            val foundProvidedPorts = componentType.getProvided.filter(p => p.getName == genName)
            if (!foundProvidedPorts.isEmpty) {
              foundProvidedPorts.foreach {
                ptref => {
                  val ptREFmapping = KevoreeFactory.eINSTANCE.createPortTypeMapping
                  ptREFmapping.setBeanMethodName(methoddef.getSimpleName.toString)
                  ptref.getRef match {
                    case mpt: MessagePortType => {
                      ptREFmapping.setServiceMethodName("process")
                    }
                    case spt: ServicePortType => {
                      ptREFmapping.setServiceMethodName(annot.method)
                    }
                  }
                  ptref.addMappings(ptREFmapping)
                }

              }
            } else {
              val message: String = "[PortMappingProcessor]:" + componentType.getBean + " declares a mapping to a ProvidedPort \"" + annot.name + "\", but this port has not been declared in ComponentType annotations.\nCan not resume. Process Exit.";
              env.getMessager.printMessage(Kind.ERROR, message);
            }
          }
        }



        val foundProvidedPorts = componentType.getProvided.filter(p => {
          annot.name match {
            case "*" => true
            case _@genName => p.getName == genName
          }
        })

        //  println(annot.name()+"=>"+foundProvidedPorts+"->"+componentType.getProvided.size)

        if (!foundProvidedPorts.isEmpty) {
          foundProvidedPorts.foreach {
            ptref => {
              val ptREFmapping = KevoreeFactory.eINSTANCE.createPortTypeMapping
              ptREFmapping.setBeanMethodName(methoddef.getSimpleName.toString)

              ptref.getRef match {
                case mpt: MessagePortType => {
                  ptREFmapping.setServiceMethodName("process")
                }
                case spt: ServicePortType => {
                  ptREFmapping.setServiceMethodName(annot.method)
                }

              }
              ptref.addMappings(ptREFmapping)
            }

          }
        } else {
          annot.name match {
            case "*" =>
            case _@genName => {
              val message: String = "[PortMappingProcessor]:" + componentType.getBean + " declares a mapping to a ProvidedPort \"" + annot.name + "\", but this port has not been declared in ComponentType annotations.\nCan not resume. Process Exit.";
              env.getMessager.printMessage(Kind.ERROR, message);
            }
          }
        }
    }

    /* STEP 1 : PROCESS START & STOP METHOD */
    /*
    var startAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Start])
    var stopAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Stop])
    if(startAnnot != null){ componentType.setStartMethod(methoddef.getSimpleName)}
    if(stopAnnot != null){ componentType.setStopMethod(methoddef.getSimpleName)}*/

  }

}
