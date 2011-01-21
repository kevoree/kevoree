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

package org.kevoree.merger.tests


import org.kevoree.api.service.core.merger.MergerService
import org.junit._
import org.kevoree.merger.KevoreeMergerComponent


class InitialeMergeTest extends MergerTestSuiteHelper {

  var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifyRelativePathDetected() {
    var mergedModel = model("corrupted/MergedWithRelativeReferences.art2")
    assert( !hasNoRelativeReference("corrupted","MergedWithRelativeReferences.art2") )
  }
  
  @Test def verifySimpleMerge1() {
    var mergedModel = component.merge(emptyModel, model("simple/simpleEntimidLib.art2"))
    //mergedModel testSave
    mergedModel testSave ("simple","simpleEntimidLibMerged.art2")


  }

   @Test def verifyArtFragmentBaseMerge() {
    var mergedModel = component.merge(emptyModel, model("artFragments/lib4test-base.art2"))
    //mergedModel testSave
    mergedModel testSave ("artFragments","lib4test-base-MERGED.art2")

    assert(hasNoRelativeReference("artFragments","lib4test-base-MERGED.art2"))
  }

}
