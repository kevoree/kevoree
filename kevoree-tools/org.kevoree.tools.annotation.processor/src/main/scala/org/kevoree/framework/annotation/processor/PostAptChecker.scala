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

import org.kevoree.ContainerRoot
import org.kevoree.LifeCycleTypeDefinition
import scala.collection.JavaConversions._

class PostAptChecker(root : ContainerRoot) {

  private var errors : String = ""
  private var warnings : String = ""

  def check = {
    checkLifeCycleMethods

    if( !errors.equals("")) {
      printf("========== Errors detected by the PostAptChecker ==========\n")
      printf(errors)
    }

    if( !warnings.equals("")) {
      printf("========== Warnings detected by the PostAptChecker ==========\n")
      printf(warnings)
    }

    errors.equals("")
  }

  def checkLifeCycleMethods = {

    root.getTypeDefinitions.foreach{typeDef =>
      typeDef match {

        case lctd : LifeCycleTypeDefinition => {
            if(lctd.getStartMethod == null) {
              errors += "[ERROR] in " + typeDef.getBean + "\n@Start method is mandatory.\n"
            }
            if(lctd.getStopMethod == null) {
              errors += "[ERROR] in " + typeDef.getBean + "\n@Stop method is mandatory.\n"
            }
            if(lctd.getUpdateMethod == null) {
              warnings += "[WARNING] in " + typeDef.getBean + "\n@Update method is missing.\n"
            }
          }

        case _=>
      }
    }
  }

}
