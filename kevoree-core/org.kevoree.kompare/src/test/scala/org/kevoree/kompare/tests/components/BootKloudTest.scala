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
import org.kevoree.kompare.{KevoreeKompareBean}
import org.kevoree.kompare.tests.KompareSuite
import org.scalatest.junit.AssertionsForJUnit

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 14/03/13
 * Time: 14:57
 */

class BootKloudTest extends AssertionsForJUnit with KompareSuite {
  var component: KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def verifyNoComponentMoved() {
    val kompareModel = component.kompare(emptyModel, model("test_instance/bootKloudNode1.kev"), "node1")
    kompareModel.print
  }

  @Test def verifyThirdPartyInstall() {
    val kompareModel = component.kompare(model("test_instance/preThirdParty.kev"), model("test_instance/postThirdParty.kev"), "node0")
    kompareModel.print
  }

  @Test def verify() {
    val kompareModel = component.kompare(emptyModel, model("test_instance/editor_klout.kev"), "editor_node")
    kompareModel.print
  }

  @Test def verify2() {
    val kompareModel = component.kompare(model("test_instance/model0.kev"), model("test_instance/model1.kev"),"minicloud")
    kompareModel.print
  }

  @Test def verifyChannel() {
    val kompareModel = component.kompare(emptyModel, model("test_instance/channelBoot.kev"),"minicloud")
    kompareModel.print
  }

  @Test def verifyChannel2() {
    val kompareModel = component.kompare(emptyModel, model("test_instance/channel2Boot.kev"),"node0")
    kompareModel.print
  }

  @Test def verifyChannel3() {
    val kompareModel = component.kompare(model("test_instance/channel2Boot.kev"), model("test_instance/channel3Boot.kev"),"node0")
    kompareModel.print
  }


  @Test def verifyChannelStart() {
    val kompareModel = component.kompare(emptyModel, model("test_instance/conf_ws_chan.kvm"),"node2")
    kompareModel.print
  }

  /* */
  @Test def verifyBinding() {
    val kompareModel = component.kompare(model("test_kloud/model0.kev"), model("test_kloud/model1.kev"),"node0")
    kompareModel.print
  }

  @Test def verifyDynInstance() {
    val kompareModel = component.kompare(model("test_dynInstance/initModel.kvm"), model("test_dynInstance/withChanModel.kvm"),"node0")
    kompareModel.print
  }

  @Test def verifyDynInstance2() {
    val kompareModel = component.kompare(model("test_dynInstance/initModel2.kvm"), model("test_dynInstance/withChanModel2.kvm"),"node0")
    kompareModel.print
  }

}
