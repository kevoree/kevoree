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
package org.kevoree.basechecker.tests

import org.junit.Test
import org.kevoree.core.basechecker.portchecker.PortChecker
import org.kevoree.core.basechecker.dictionaryChecker.DictionaryNetworkPortChecker

/**
 * Date: 14/10/11
 * Time: 17:51
 *
 * @version 1.0
 */

class DictionaryTest extends BaseCheckerSuite {

  @Test def checkComponentPortConflictDetection () {
    val modelPort = model("test_checker/dictionary/comportPortConflict.kev")
    val dChecker = new DictionaryNetworkPortChecker
    val res = dChecker.check(modelPort)
    assert(res.size().equals(1))
  }

  @Test def checkComponentPortNoConflictDetection () {
    val modelPort = model("test_checker/dictionary/comportPortNoConflict.kev")
    val dChecker = new DictionaryNetworkPortChecker
    val res = dChecker.check(modelPort)
    assert(res.isEmpty)
  }

  @Test def checkComponentSubPortConflictDetection () {
    val modelPort = model("test_checker/dictionary/comportSubPortConflict.kev")
    val dChecker = new DictionaryNetworkPortChecker
    val res = dChecker.check(modelPort)
    assert(res.size().equals(1))
  }

  @Test def checkComponentSubPortNoConflictDetection () {
    val modelPort = model("test_checker/dictionary/comportSubPortNoConflict.kev")
    val dChecker = new DictionaryNetworkPortChecker
    val res = dChecker.check(modelPort)
    assert(res.isEmpty)
  }

  @Test def checkChannelSubPortConflictDetection () {
    val modelPort = model("test_checker/dictionary/channelSubPortConflict.kev")
    val dChecker = new DictionaryNetworkPortChecker
    val res = dChecker.check(modelPort)
    assert(res.size().equals(1))
  }

  @Test def checkChannelSubPortNoConflictDetection () {
    val modelPort = model("test_checker/dictionary/channelSubPortNoConflict.kev")
    val dChecker = new DictionaryNetworkPortChecker
    val res = dChecker.check(modelPort)
    assert(res.isEmpty)
  }

}