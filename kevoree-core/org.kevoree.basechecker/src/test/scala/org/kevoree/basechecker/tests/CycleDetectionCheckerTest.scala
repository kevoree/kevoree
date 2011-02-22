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

package org.kevoree.basechecker.tests

import org.kevoree.core.basechecker.cyclechecker.CycleChecker
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.JavaConversions._
import org.junit._

class CycleDetectionCheckerTest extends AssertionsForJUnit with BaseCheckerSuite {

  @Test def verifyCycleDetectionOK() {

    
    var modelCycle = model("test_checker/model_cycle_depth.kev")
    var cycleChecker = new CycleChecker
    var firstTime = System.currentTimeMillis
    var res = cycleChecker.check(modelCycle)
    println(System.currentTimeMillis-firstTime+"ms")

    res.foreach{violation =>
      println(violation.getMessage)
    }

    assert(res.size > 0)

    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    // error("NOT IMPLEMENTED YET");
  }


  @Test def verifyNoCycleDetection() {

    var modelCycle = model("test_checker/model_noCycle.kev")
    var cycleChecker = new CycleChecker
    var firstTime = System.currentTimeMillis
    var res = cycleChecker.check(modelCycle)
    println(System.currentTimeMillis-firstTime+"ms")
    res.foreach{violation =>
      println(violation.getMessage)
    }

    assert(res.size == 0)

  }

}
