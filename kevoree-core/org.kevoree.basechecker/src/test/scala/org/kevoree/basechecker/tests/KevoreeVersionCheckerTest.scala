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

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import collection.JavaConversions._
import org.kevoree.core.basechecker.kevoreeVersionChecker.{KevoreeNodeVersionChecker, KevoreeVersionChecker}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/04/12
 * Time: 16:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeVersionCheckerTest extends AssertionsForJUnit with BaseCheckerSuite {

  @Test def checkKevoreeNodeVersionOK () {
    val m = model("test_checker/kevoreeVersionChecker/validModel.kev")
    val kevoreeVersionChecker = new KevoreeNodeVersionChecker("node0")
    val violations = kevoreeVersionChecker.check(m)
    assert(violations.size().equals(0))
  }

  @Test def checkKevoreeNodeVersionKO () {
    val m = model("test_checker/kevoreeVersionChecker/invalidModel4.kev")
    val kevoreeVersionChecker = new KevoreeNodeVersionChecker("node0")
    val violations = kevoreeVersionChecker.check(m)
    /*violations.foreach {
      v => println(v.getMessage)
    }*/
    assert(violations.size().equals(1))
  }

  @Test def checkKevoreeVersionOK () {
    val m = model("test_checker/kevoreeVersionChecker/validModel.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    assert(violations.size().equals(0))
  }

  @Test def checkKevoreeVersionKO () {
    val m = model("test_checker/kevoreeVersionChecker/invalidModel.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    /*violations.foreach {
      v => println(v.getMessage)
    }*/
    assert(violations.size().equals(2))
  }

  @Test def checkKevoreeVersionKO2 () {
    val m = model("test_checker/kevoreeVersionChecker/invalidModel2.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    /*violations.foreach {
      v => println(v.getMessage)
    }*/
    assert(violations.size().equals(2))
  }

  @Test def checkKevoreeVersionKO3 () {
    val m = model("test_checker/kevoreeVersionChecker/invalidModel3.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    /*violations.foreach {
            v => println(v.getMessage)
          }*/
    assert(violations.size().equals(1))
  }
}
