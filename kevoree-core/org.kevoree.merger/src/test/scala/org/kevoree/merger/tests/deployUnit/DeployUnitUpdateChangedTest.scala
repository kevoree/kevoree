package org.kevoree.merger.tests.deployUnit

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

import org.junit._
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.api.service.core.merger.MergerService

class DeployUnitUpdateChangedTest extends MergerTestSuiteHelper {

  var component: MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifyComponentTypeAdded() {
    val mergedModel = component.merge(model("deployUnit/modelForgotProvidedDeployUnit.kev"), model("deployUnit/modelForgotProvidedDeployUnitFix.kev"))
    mergedModel.testSave("deployUnit", "checkDeployUnitUpdateWihtoutContractChanged.kev")

    println("Hello")

     mergedModel.getDeployUnits.foreach{ du =>
       println(du.getUnitName+"=>"+du.getRequiredLibs.size())
     }

        /*
    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentB")
    ) match {
      case None => fail("ComponentB have not been properly added.")
      case Some(component) =>
    }    */
  }

}
