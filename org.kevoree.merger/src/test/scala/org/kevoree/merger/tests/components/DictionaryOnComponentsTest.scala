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

package org.kevoree.merger.tests.components

import org.junit._
import org.kevoree.api.service.core.merger.MergerService
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoreeAdaptation._
import org.scalatest.junit.AssertionsForJUnit

class DictionaryOnComponentsTest extends MergerTestSuiteHelper  {

   var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifyUpdateValue() {
    //var mergedModel = component.merge(emptyModel, model("simple/simpleEntimidLib.art2"))
    //mergedModel testSave
    //mergedModel testSave ("simple","simpleEntimidLibMerged.art2")
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyNotUpdateValue() {
    //var mergedModel = component.merge(emptyModel, model("simple/simpleEntimidLib.art2"))
    //mergedModel testSave
    //mergedModel testSave ("simple","simpleEntimidLibMerged.art2")
    //error("NOT IMPLEMENTED YET")
  }

}
