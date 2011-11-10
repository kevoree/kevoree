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
import org.kevoree.core.basechecker.cyclechecker.ComponentCycleChecker
import org.kevoree.core.basechecker.nodechecker.NodeChecker
import org.scalatest.Assertions._
import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/11/11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */

class DeployUnitCheckerTest extends AssertionsForJUnit with BaseCheckerSuite {

  @Test def verifyCycleDetectionOK() {


 		val m = model("test_checker/nodeployunit/nodeployNode.kev")
    val nodeChecker = new NodeChecker
    val violations = nodeChecker.check(m)
    assert(violations.size().equals(1))


    val m2 = model("test_checker/nodeployunit/nodeployNodeOk.kev")
    val violations2 = nodeChecker.check(m2)
    assert(violations2.size().equals(0))
    
    /*
    val m3 = KevoreeXmiHelper.load("/Users/duke/Desktop/drop.kev")
    val violations3 = nodeChecker.check(m3)
    
    import scala.collection.JavaConversions._
    violations3.foreach{
      v => println(v.getMessage)
    }

    println(violations3.size())
    */
  }
  
}