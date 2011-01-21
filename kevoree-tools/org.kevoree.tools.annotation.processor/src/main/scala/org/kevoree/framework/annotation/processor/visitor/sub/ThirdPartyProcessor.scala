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

import com.sun.mirror.declaration.ClassDeclaration
import com.sun.mirror.declaration.TypeDeclaration
import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._


/* Common Sub process to deal with ThirdParty definition */

trait ThirdPartyProcessor {

  def processThirdParty(componentType : TypeDefinition,classdef : TypeDeclaration)={
    var root : ContainerRoot = componentType.eContainer.asInstanceOf[ContainerRoot]

     var thirdPartyAnnotations : List[org.kevoree.annotation.ThirdParty] = Nil

    var annotationThirdParty = classdef.getAnnotation(classOf[org.kevoree.annotation.ThirdParty])
    if(annotationThirdParty != null){ thirdPartyAnnotations = thirdPartyAnnotations ++ List(annotationThirdParty) }

    var annotationThirdParties = classdef.getAnnotation(classOf[org.kevoree.annotation.ThirdParties])
    if(annotationThirdParties != null){ thirdPartyAnnotations = thirdPartyAnnotations ++ annotationThirdParties.value.toList }



    /* CHECK THIRDPARTIES */
    thirdPartyAnnotations.foreach{tp=>
      
        root.getDeployUnits.find({etp => etp.getName == tp.name}) match {
          case Some(e) => {
              componentType.getRequiredLibs.add(e)
            }
          case None => {
              var newThirdParty = KevoreeFactory.eINSTANCE.createDeployUnit
              newThirdParty.setName(tp.name)
              newThirdParty.setUrl(tp.url)
              root.getDeployUnits.add(newThirdParty)
              componentType.getRequiredLibs.add(newThirdParty)
            }
        }
    }
  }
}
