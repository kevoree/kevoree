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
import org.kevoree.KevoreeFactory
import org.kevoree.ComponentType
import org.kevoree.framework.annotation.processor.LocalUtility
import org.kevoree.framework.annotation.processor.visitor.ServicePortTypeVisitor
import scala.collection.JavaConversions._

trait ProvidedPortProcessor {

  def processProvidedPort(componentType : ComponentType,classdef : TypeDeclaration, env : AnnotationProcessorEnvironment)={
    /* CHECK PROVIDED PORTS */
    if(classdef.getAnnotation(classOf[org.kevoree.annotation.Provides]) != null){
      classdef.getAnnotation(classOf[org.kevoree.annotation.Provides]).value.foreach{req=>

        var portAll : List[org.kevoree.PortTypeRef] = componentType.getRequired.toList ++ componentType.getProvided.toList
        portAll.find(alR=> alR.getName == req.name) match {
          case None => {
              var ptreqREF = KevoreeFactory.eINSTANCE.createPortTypeRef
              ptreqREF.setName(req.name)

              ptreqREF.setRef(LocalUtility.getOraddPortType(req.`type` match {
                    case org.kevoree.annotation.PortType.SERVICE => {
                        var tv = new ServicePortTypeVisitor
                        try { req.className
                        } catch {case e : com.sun.mirror.`type`.MirroredTypeException => e.getTypeMirror.accept(tv)}
                        tv.getDataType
                      }
                    case org.kevoree.annotation.PortType.MESSAGE => {
                        var mpt = KevoreeFactory.eINSTANCE.createMessagePortType
                        mpt.setName("org.kevoree.framework.MessagePort")
                        req.filter.foreach{ndts=>
                          var ndt = KevoreeFactory.eINSTANCE.createTypedElement
                          ndt.setName(ndts)
                          mpt.getFilters.add(LocalUtility.getOraddDataType(ndt))
                        }
                        mpt
                      }
                    case _ => null
                  }))

              componentType.getProvided.add(ptreqREF)
            }
          case Some(e)=> {
              env.getMessager.printError("Port name duplicated in "+componentType.getName+" Scope => "+req.name)
            }
        }



      }
    }
  }

}
