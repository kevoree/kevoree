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

package org.kevoree.merger.tests.ports

import org.junit._
import org.kevoree.api.service.core.merger.MergerService
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoreeAdaptation._
import org.scalatest.junit.AssertionsForJUnit

class DictionaryOnPortsTest extends MergerTestSuiteHelper  {

   var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }


  @Test def verifyDictionnaryAddedOnMessagePort() {

/*    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-PlusServiceDictionaries.art2"))
    mergedModel testSave ("artFragments","lib4test-PlusServiceDictionariesMerged.art2")
*/
    //error("NOT IMPLEMENTED YET")

  }

  @Test def verifyDictionnaryAddedOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAddedOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryRemovedOnMessagePort() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryRemovedOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryRemovedOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }

    @Test def verifyDictionnaryAttributeRemovedOnMessagePort() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeRemovedOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeRemovedOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeAddedOnMessagePort() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeAddedOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeAddedOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }

    @Test def verifyDictionnaryAttributeRenamedOnMessagePort() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeRenamedOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeRenamedOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }

    @Test def verifyDictionnaryAttributeMandatoryUpdateOnMessagePort() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeMandatoryUpdateOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeMandatoryUpdateOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }

      @Test def verifyDictionnaryAttributeDefaultValueUpdateOnMessagePort() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeDefaultValueUpdateOnServicePortMethodFromApi() {
    //error("NOT IMPLEMENTED YET")
  }

  @Test def verifyDictionnaryAttributeDefaultValueUpdateOnServicePortMethodFromImpl() {
    //error("NOT IMPLEMENTED YET")
  }
}
