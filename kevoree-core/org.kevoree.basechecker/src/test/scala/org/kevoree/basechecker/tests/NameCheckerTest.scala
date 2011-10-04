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
import org.kevoree.core.basechecker.namechecker.NameChecker
import scala.collection.JavaConversions._


class NameCheckerTest extends AssertionsForJUnit with BaseCheckerSuite {

	@Test def verifyNodeNameOK() {
		val nodeModel = model("test_checker/name/model_single_nodeOK.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	@Test def verifyNodeNameKO() {
		val nodeModel = model("test_checker/name/model_single_nodeKO.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	@Test def verifyNodesNameOK() {
		val nodeModel = model("test_checker/name/model_two_nodeOK.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	@Test def verifyNodesNameKO() {
		val nodeModel = model("test_checker/name/model_two_nodeKO.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	@Test def verifyNodeAndComponentNameOK() {
		val nodeModel = model("test_checker/name/model_1node_1componentOK.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	@Test def verifyNodeAndComponentNameKO() {
		val nodeModel = model("test_checker/name/model_1node_1componentKO.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	@Test def verifyChannelNameKO() {
		val nodeModel = model("test_checker/name/model_1node_1channelKO.kev")
		val nodeNameChecker = new NameChecker
		val firstTime = System.currentTimeMillis
		val res = nodeNameChecker.check(nodeModel)
		println(System.currentTimeMillis - firstTime + "ms for name checking")

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

	// TODO test port name checking
	// TODO test attribute name checking with wrong attribute name
}
