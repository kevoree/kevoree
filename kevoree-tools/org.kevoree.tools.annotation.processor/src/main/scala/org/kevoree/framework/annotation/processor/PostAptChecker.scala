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
import scala.collection.JavaConversions._
import org.kevoree._

class PostAptChecker(root: ContainerRoot, env: AnnotationProcessorEnvironment) {

  private var errors = 0

  def check = {
    checkComponentTypes()
    errors == 0

  }

  def checkComponentTypes() {

    root.getTypeDefinitions.foreach {
      typeDef =>
        checkLifeCycleMethods(typeDef)
        checkPackage(typeDef)
    }
  }

  private def checkLifeCycleMethods(td: TypeDefinition) {
    td match {

      case ntype: NodeType => //IGNORE CHECK FOR NODE TYPE

      case lctd: LifeCycleTypeDefinition => {
        if (lctd.getStartMethod == null) {
          env.getMessager.printError("@Start method is mandatory in " + td.getBean + "." + "\n")
          errors += 1
        }
        if (lctd.getStopMethod == null) {
          env.getMessager.printError("@Stop method is mandatory in " + td.getBean + "." + "\n")
          errors += 1
        }
        if (lctd.getUpdateMethod == null) {
          env.getMessager.printWarning("@Update method is missing in " + td.getBean + "." + "\n")
        }

      }

      case _ =>
    }
  }

  private def checkPackage(td: TypeDefinition) {
    td match {
      case ct: ComponentType => {
        if (td.getBean == null) {
          env.getMessager.printError("TypeDefinition bean is null for " + td.getName)
          errors += 1
        } else {
          if (td.getBean.lastIndexOf(".") == -1) {
            env.getMessager.printError("The TypeDefinition seems to be out of any package. (lastIndexOf('.') returned -1 for bean : " + td.getBean + "\n")
            errors += 1
          }
        }
        if (td.getFactoryBean.lastIndexOf(".") == -1) {
          env.getMessager.printError("The TypeDefinition seems to be out of any package. (lastIndexOf('.') returned -1 for FactoryBean : " + td.getFactoryBean + "\n")
          errors += 1
        }
      }
      case _ =>
    }

  }

}
