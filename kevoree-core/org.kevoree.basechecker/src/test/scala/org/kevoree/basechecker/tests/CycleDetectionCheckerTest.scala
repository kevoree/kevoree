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
		val modelCycle = model("test_checker/model_cycle_depth.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 0)
		if (res.size == 1) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}


	@Test def verifyNoCycleDetection() {

		val modelCycle = model("test_checker/model_noCycle.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 0)
		if (res.size == 0) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}

	@Test def verifyNoDistributedAndLocalCycleDetectionWith2Nodes() {
		val modelCycle = model("test_checker/distributed_test/model_no_cycles.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 0)
		if (res.size == 0) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}

	@Test def verifyNoDistributedAndLocalCycleDetectionWith3Nodes() {
		val modelCycle = model("test_checker/distributed_test/model_no_cycles_3nodes.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 0)
		if (res.size == 0) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}

	@Test def verifyDistributedCycleDetectionWith2Nodes() {
		val modelCycle = model("test_checker/distributed_test/model_simple_distributed_cycles.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 1)
		if (res.size == 1) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}

	@Test def verifyDistributedCycleDetectionWith3Nodes() {
		val modelCycle = model("test_checker/distributed_test/model_simple_distributed_cycles_3nodes.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 1)
		if (res.size == 1) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}

	@Test def verifyDistributedAndLocalCycleDetection() {
		val modelCycle = model("test_checker/distributed_test/model_distributed_cycles_between_3nodes+local_cycle.kev")
		val cycleChecker = new CycleChecker
		val firstTime = System.currentTimeMillis
		val res = cycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		//assert(res.size == 2)
		if (res.size == 2) {
			assert(true)
		} else {
			res.foreach {
				violation =>
					println(violation.getMessage)
					violation.getTargetObjects.foreach {
						obj =>
							println(obj)
					}
			}
			assert(false)
		}
	}
}
