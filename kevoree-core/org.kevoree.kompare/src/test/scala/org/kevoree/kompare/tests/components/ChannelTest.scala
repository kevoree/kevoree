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
package org.kevoree.kompare.tests.components

import org.junit.{Test, Before}
import org.kevoree.kompare.KevoreeKompareBean
import org.kevoree.kompare.tests.KompareSuite
import org.scalatest.junit.AssertionsForJUnit

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 14/03/13
 * Time: 14:57
 */

class ChannelTest extends AssertionsForJUnit with KompareSuite {
  var component: KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def verifyNoComponentMoved() {
    var kompareModel = component.kompare(emptyModel, model("test_instance/testChannelInit.kev"), "node0")
    kompareModel.print
  }


}
