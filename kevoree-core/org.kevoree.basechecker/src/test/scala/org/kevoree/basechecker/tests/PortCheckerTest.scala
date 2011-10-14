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

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/10/11
 * Time: 17:51
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class PortCheckerTest extends BaseCheckerSuite {

  @Test def checkRequiredPort () {
    val modelPort = model("test_checker/requiredPort/testPortChecker3.kev")
    val portChecker = new PortChecker
    assert(portChecker.check(modelPort).size().equals(2))
  }

}