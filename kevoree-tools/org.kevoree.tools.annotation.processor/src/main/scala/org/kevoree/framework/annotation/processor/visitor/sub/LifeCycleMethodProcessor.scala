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

import com.sun.mirror.declaration.MethodDeclaration
import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.TypeDefinition

trait LifeCycleMethodProcessor {

  def processLifeCycleMethod(typeDefinition : TypeDefinition,methoddef : MethodDeclaration)={

    typeDefinition match {

      case c: ChannelType => {
          /* STEP 1 : PROCESS START & STOP METHOD */
          var startAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Start])
          var stopAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Stop])
          if(startAnnot != null){ c.setStartMethod(methoddef.getSimpleName)}
          if(stopAnnot != null){ c.setStopMethod(methoddef.getSimpleName)}
        }
      case c : ComponentType => {
          var startAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Start])
          var stopAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Stop])
          if(startAnnot != null){ c.setStartMethod(methoddef.getSimpleName)}
          if(stopAnnot != null){ c.setStopMethod(methoddef.getSimpleName)}
        }
      case _ =>

    }


  }

}
