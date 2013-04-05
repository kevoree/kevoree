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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.kompare.tests.components

import org.junit._
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.kompare.tests._
import org.kevoree.kompare.{JavaSePrimitive, KevoreeKompareBean}

class DictionaryOnComponentsTest extends AssertionsForJUnit with KompareSuite {

  var component : KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def verifyUpdateValue() {
    val kompareModel = component.kompare(model("tests_dictionary/dictionary_1.kev"), model("tests_dictionary/dictionary_2.kev"), "node-0")
    kompareModel.print
    kompareModel verifySize 1
    kompareModel shouldContain(JavaSePrimitive.instance$.getUpdateDictionaryInstance,"FakeSimpleLight--398723264")
  }

  @Test def verifyNotUpdateValue() {
    val kompareModel = component.kompare(model("tests_dictionary/dictionary_1.kev"), model("tests_dictionary/dictionary_1.kev"), "node-0")
    kompareModel.shouldNotContain(JavaSePrimitive.instance$.getUpdateDictionaryInstance)
    kompareModel verifySize 0
  }

}
