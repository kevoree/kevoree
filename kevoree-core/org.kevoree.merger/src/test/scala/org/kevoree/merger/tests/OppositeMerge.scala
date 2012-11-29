/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.merger.tests

import org.junit.{Test, Before}
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.api.service.core.merger.MergerService

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/12
 * Time: 07:29
 * To change this template use File | Settings | File Templates.
 */
class OppositeMerge extends MergerTestSuiteHelper {

  var component: MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifySimpleMerge1() {
    var mergedModel = component.merge(emptyModel, model("kloud/k1.kev"))
    //mergedModel testSave
    mergedModel testSave ("kloud","k1m.kev")
  }

}
