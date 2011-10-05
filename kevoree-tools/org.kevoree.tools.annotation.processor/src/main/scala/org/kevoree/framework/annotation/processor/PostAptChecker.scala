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

import org.kevoree._
import core.basechecker.RootChecker

class PostAptChecker(root: ContainerRoot, env: AnnotationProcessorEnvironment) {

  private var nbErrors = 0

  def check = {
    baseCheck()
    checkComponentTypes()
    nbErrors == 0
  }

  def baseCheck() {
    val baseChecker = new RootChecker
    val errors = baseChecker.check(root)
    if(errors.size() != 0) {
      import scala.collection.JavaConversions._
      errors.foreach{error =>
        env.getMessager.printError(error.getMessage)
        nbErrors += 1
      }
    }
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
        if (lctd.getStartMethod == "") {
          env.getMessager.printError("@Start method is mandatory in " + td.getBean + "." + "\n")
          nbErrors += 1
        }
        if (lctd.getStopMethod == "") {
          env.getMessager.printError("@Stop method is mandatory in " + td.getBean + "." + "\n")
          nbErrors += 1
        }
        if (lctd.getUpdateMethod == "") {
          env.getMessager.printWarning("@Update method is missing in " + td.getBean + "." + "\n")
        }

      }

      case _ =>
    }
  }

  private def checkPackage(td: TypeDefinition) {
    td match {
      case ct: ComponentType => {
        if (td.getBean == "") {
          env.getMessager.printError("TypeDefinition bean is null for " + td.getName)
          nbErrors += 1
        } else {
          if (td.getBean.lastIndexOf(".") == -1) {
            env.getMessager.printError("The TypeDefinition seems to be out of any package. (lastIndexOf('.') returned -1 for bean : " + td.getBean + "\n")
            nbErrors += 1
          }
        }
        if (td.getFactoryBean.lastIndexOf(".") == -1) {
          env.getMessager.printError("The TypeDefinition seems to be out of any package. (lastIndexOf('.') returned -1 for FactoryBean : " + td.getFactoryBean + "\n")
          nbErrors += 1
        }
      }
      case _ =>
    }

  }

}
