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
package org.kevoree.kompare.tests.group

import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.kompare.tests.KompareSuite
import org.scalatest.AbstractSuite
import org.junit._
import org.kevoreeadaptation._
import org.kevoree.kompare.{JavaSePrimitive, KevoreeKompareBean}

class GroupUpdateTest extends AssertionsForJUnit with KompareSuite {
  var component: KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def verifyUPDATE_ADDGROUP() {

    val kompareModel = component.kompare(model("test_instance/groupNoGroup.kev"), model("test_instance/groupInitAddOneTwoBinding.kev"), "duke")
    kompareModel.shouldContain(JavaSePrimitive.instance$.getAddType, "GossipGroup")
    kompareModel.shouldContain(JavaSePrimitive.instance$.getAddInstance, "group1426020324")
  }

  @Test def verifyUPDATE_RemoveGROUP() {
    val kompareModel = component.kompare(model("test_instance/groupInitAddOneTwoBinding.kev"), model("test_instance/groupNoGroup.kev"), "duke")
    kompareModel.shouldContain(JavaSePrimitive.instance$.getRemoveType, "GossipGroup")
    kompareModel.shouldContain(JavaSePrimitive.instance$.getRemoveInstance, "group1426020324")
  }


  @Test def verifyUPDATE_UpdateGroupAddBinding() {
    val kompareModel = component.kompare(model("test_instance/groupOneBinding.kev"), model("test_instance/groupInitAddOneTwoBinding.kev"), "duke")
    kompareModel.print
    kompareModel.shouldContain(JavaSePrimitive.instance$.getUpdateDictionaryInstance, "group1426020324")
  }

  @Test def verifyUPDATE_UpdateGroupRemoveBinding() {
    val kompareModel = component.kompare(model("test_instance/groupInitAddOneTwoBinding.kev"), model("test_instance/groupOneBinding.kev"), "duke")
    kompareModel.shouldContain(JavaSePrimitive.instance$.getUpdateDictionaryInstance, "group1426020324")
  }

}