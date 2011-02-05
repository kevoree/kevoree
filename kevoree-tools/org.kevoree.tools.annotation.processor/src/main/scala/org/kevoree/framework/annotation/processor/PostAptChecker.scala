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

package org.kevoree.framework.annotation.processor

import com.sun.mirror.apt.AnnotationProcessorEnvironment
import org.kevoree.ContainerRoot
import org.kevoree.LifeCycleTypeDefinition
import scala.collection.JavaConversions._

class PostAptChecker(root : ContainerRoot, env : AnnotationProcessorEnvironment) {

  private var errors = 0

  def check = {
    checkLifeCycleMethods
    errors == 0
  }

  def checkLifeCycleMethods = {

    root.getTypeDefinitions.foreach{typeDef =>
      typeDef match {

        case lctd : LifeCycleTypeDefinition => {
            if(lctd.getStartMethod == null) {
              env.getMessager.printError("@Start method is mandatory in " + typeDef.getBean + ".")
              errors += 1
            }
            if(lctd.getStopMethod == null) {
              env.getMessager.printError("@Stop method is mandatory in " + typeDef.getBean + ".")
              errors += 1
            }
            if(lctd.getUpdateMethod == null) {
              env.getMessager.printWarning("@Update method is missing in " + typeDef.getBean + ".")
            }

          }

        case _=>
      }
    }
  }

}
