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

package org.kevoree.framework.annotation.processor.visitor.sub

import org.kevoree.LifeCycleTypeDefinition
import org.kevoree.TypeDefinition
import javax.lang.model.element.ExecutableElement

trait LifeCycleMethodProcessor {

  def processLifeCycleMethod(typeDefinition : TypeDefinition,methoddef : ExecutableElement)={

    typeDefinition match {

      case lctd : LifeCycleTypeDefinition =>{
          /* STEP 1 : PROCESS START & STOP METHOD */
          val startAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Start])
          val stopAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Stop])
          val updateAnnot = methoddef.getAnnotation(classOf[org.kevoree.annotation.Update])
          if(startAnnot != null){ lctd.setStartMethod(methoddef.getSimpleName.toString)}
          if(stopAnnot != null){ lctd.setStopMethod(methoddef.getSimpleName.toString)}
          if(updateAnnot != null){ lctd.setUpdateMethod(methoddef.getSimpleName.toString)}
        }
/*
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
        }*/
      case _ =>

    }


  }

}
