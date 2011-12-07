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

import org.kevoree._
import core.basechecker.RootChecker
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic.Kind

class PostAptChecker(root: ContainerRoot, env: ProcessingEnvironment) {

  private var nbErrors = 0

  def check = {
    checkComponentTypes()
    mappingsCheck()
    baseCheck()
    nbErrors == 0
  }

    def mappingsCheck() {
    root.getTypeDefinitions.filter(td => td.isInstanceOf[ComponentType]).foreach{ ct =>
      ct.asInstanceOf[ComponentType].getProvided.foreach{providedPortTypeRef =>
        providedPortTypeRef.getRef match {
          case mpt : MessagePortType => {
            if (!(providedPortTypeRef.getMappings.size > 0)) {
              env.getMessager.printMessage(Kind.ERROR, "The process method of port: " + providedPortTypeRef.getName + ":MessagePort is not mapped on any method. Please, complete your code.")
              nbErrors += 1
            }
          }
          case spt : ServicePortType => {
            spt.getOperations.foreach{op=>
              if(providedPortTypeRef.getMappings.find(ptmap => ptmap.getServiceMethodName.equals(op.getName)).isEmpty) {
                env.getMessager.printMessage(Kind.ERROR, "The method " + op.getName + " of port " + providedPortTypeRef.getName + ":" + spt.getName + " is not mapped on any method. Please, complete your code.")
                nbErrors += 1
              }
            }
            }
        }
      }
    }
  }

  def baseCheck() {
    val baseChecker = new RootChecker
    val errors = baseChecker.check(root)
    if(errors.size() != 0) {
      import scala.collection.JavaConversions._
      errors.foreach{error =>
        env.getMessager.printMessage(Kind.ERROR,error.getMessage)
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
    
    if(td.getBean == ""){
         return
    }
    
    td match {

      case ntype: NodeType => //IGNORE CHECK FOR NODE TYPE

      case lctd: LifeCycleTypeDefinition => {
        if (lctd.getStartMethod == "") {
          env.getMessager.printMessage(Kind.ERROR,"@Start method is mandatory in " + td.getBean + ".")
          nbErrors += 1
        }
        if (lctd.getStopMethod == "") {
          env.getMessager.printMessage(Kind.ERROR,"@Stop method is mandatory in " + td.getBean + ".")
          nbErrors += 1
        }
        if (lctd.getUpdateMethod == "") {
          env.getMessager.printMessage(Kind.WARNING,"@Update method is missing in " + td.getBean + ".")
        }

      }

      case _ =>
    }
  }

  private def checkPackage(td: TypeDefinition) {
    td match {
      case ct: ComponentType => {
        if (td.getBean == "") {
          env.getMessager.printMessage(Kind.ERROR,"TypeDefinition bean is null for " + td.getName)
          nbErrors += 1
        } else {
          if (td.getBean.lastIndexOf(".") == -1) {
            env.getMessager.printMessage(Kind.ERROR,"The TypeDefinition seems to be out of any package. (lastIndexOf('.') returned -1 for bean : " + td.getBean)
            nbErrors += 1
          }
        }
        if (td.getFactoryBean.lastIndexOf(".") == -1) {
          env.getMessager.printMessage(Kind.ERROR,"The TypeDefinition seems to be out of any package. (lastIndexOf('.') returned -1 for FactoryBean : " + td.getFactoryBean)
          nbErrors += 1
        }
      }
      case _ =>
    }

  }

}
