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

import org.scalatest.junit.AssertionsForJUnit
 import org.junit._
import org.kevoree.core.basechecker.cyclechecker.{ComponentCycleChecker, NodeCycleChecker}

class CycleDetectionCheckerTest extends AssertionsForJUnit with BaseCheckerSuite {

	@Test def verifyCycleDetectionOK() {
		val modelCycle = model("test_checker/cycle/model_cycle_depth.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		val res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		if (res.size == 1) {
			return
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

		val modelCycle = model("test_checker/cycle/model_noCycle.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		val res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms")

		if (res.size == 0) {
			return
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
		val modelCycle = model("test_checker/cycle/distributed_test/model_no_cycles.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		if (res.size == 0) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 0) {
				return
			}
		}

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

	@Test def verifyNoDistributedAndLocalCycleDetectionWith2Nodes2() {
		val modelCycle = model("test_checker/cycle/distributed_test/model_no_cycles2.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		if (res.size == 0) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 0) {
				return
			}
		}

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

	@Test def verifyNoDistributedAndLocalCycleDetectionWith3Nodes() {
		val modelCycle = model("test_checker/cycle/distributed_test/model_no_cycles_3nodes.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		if (res.size == 0) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 0) {
				return
			}
		}

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

	@Test def verifyDistributedCycleDetectionWith2Nodes() {
		val modelCycle = model("test_checker/cycle/distributed_test/model_simple_distributed_cycles.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		if (res.size == 0) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 1) {
				return
			}
		}

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

	@Test def verifyDistributedCycleDetectionWith3Nodes() {
		val modelCycle = model("test_checker/cycle/distributed_test/model_simple_distributed_cycles_3nodes.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		if (res.size == 0) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 1) {
				return
			}
		}

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

	@Test def verifyDistributedCycleDetectionWith3Nodes2() {
		val modelCycle = model("test_checker/cycle/distributed_test/model_distributed_cycles_between_3nodes2.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		if (res.size == 0) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 1) {
				return
			}
		}

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


	@Test def verifyDistributedAndLocalCycleDetection() {
		val modelCycle = model("test_checker/cycle/distributed_test/model_distributed_cycles_between_3nodes+local_cycle.kev")
		val componentCycleChecker = new ComponentCycleChecker
		val firstTime = System.currentTimeMillis
		var res = componentCycleChecker.check(modelCycle)
		println(System.currentTimeMillis - firstTime + "ms for component cycle detection")

		/*res.foreach {
			violation =>
				println(violation.getMessage)
				violation.getTargetObjects.foreach {
					obj =>
						println(obj)
				}
		}*/
		if (res.size == 2) {
			val nodeCycleChecker = new NodeCycleChecker
			val firstTime = System.currentTimeMillis
			res = nodeCycleChecker.check(modelCycle)
			println(System.currentTimeMillis - firstTime + "ms for node cycle detection")
			if (res.size == 1) {
				/*res.foreach {
			violation =>
				println(violation.getMessage)
				violation.getTargetObjects.foreach {
					obj =>
						println(obj)
				}
			}*/
				return
			}
		}

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
