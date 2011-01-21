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
import com.sun.mirror.declaration.MethodDeclaration
import org.kevoree.KevoreeFactory
import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.kevoree.ServicePortType
import scala.collection.JavaConversions._

trait PortMappingProcessor {

  def processPortMapping(componentType : ComponentType,methoddef : MethodDeclaration,env : AnnotationProcessorEnvironment)={
    /* PROCESS PORTS & PORT ANNOTATION */
    var portAnnotations : List[org.kevoree.annotation.Port] = Nil

    var annotationPort = methoddef.getAnnotation(classOf[org.kevoree.annotation.Port])
    if(annotationPort != null){ portAnnotations = portAnnotations ++ List(annotationPort) }

    var annotationPorts = methoddef.getAnnotation(classOf[org.kevoree.annotation.Ports])
    if(annotationPorts != null){ portAnnotations = portAnnotations ++ annotationPorts.value.toList }

    portAnnotations.foreach{annot=>
      componentType.getProvided.find({provided=> provided.getName.equals(annot.name) }) match {
        case Some(ptref) => {
            var ptREFmapping = KevoreeFactory.eINSTANCE.createPortTypeMapping
            ptREFmapping.setBeanMethodName(methoddef.getSimpleName)

            ptref.getRef match {
              case mpt : MessagePortType => {
                  ptREFmapping.setServiceMethodName("process")
                }
              case spt : ServicePortType => {
                  ptREFmapping.setServiceMethodName(annot.method)
                }

            }
            ptref.getMappings.add(ptREFmapping)
          }
        case None => println("ProvidedPort not found "+annot.name);env.getMessager.printError("ProvidedPort not found "+annot.name);System.exit(1)
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
