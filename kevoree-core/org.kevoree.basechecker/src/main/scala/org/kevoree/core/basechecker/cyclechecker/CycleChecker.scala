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

package org.kevoree.core.basechecker.cyclechecker

import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.kevoree.ContainerRoot
import org.kevoree.MBinding
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import scala.collection.JavaConversions._

class CycleChecker extends CheckerService {

	def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
		var violations: List[CheckerViolation] = List()
		model.getNodes.foreach {
			node =>
				val graph = KevoreeComponentDirectedGraph(model, node.getName)
				/*var cycleDetector  = new CycleDetector[Instance, MBinding](graph)
											 if (cycleDetector.detectCycles) {
											 var violation = new CheckerViolation
											 violation.setMessage("Cycle(s) detected")
											 violation.setTargetObject(cycleDetector.findCycles.toList)
											 violations = violations ++ List(violation)
											 cycleDetector.findCycles
											 }*/
				violations = violations ++ check(graph)
		}

		val distributedChecking = true // TODO need to be fixed according to the model and bindings between nodes
		if (distributedChecking && model.getNodes.size > 1) {
			val graph1 = KevoreeNodeDirectedGraph(model)
			violations = violations ++ check(graph1)
		}

		violations
	}

	private def check[A](graph: DirectedGraph[A, MBinding]): List[CheckerViolation] = {
		var violations: List[CheckerViolation] = List()
		val cycleDetector = new CycleDetector[A, MBinding](graph)

		if (cycleDetector.detectCycles) {
			val violation = new CheckerViolation
			violation.setMessage("Cycle(s) detected")
			violation.setTargetObjects(cycleDetector.findCycles.asInstanceOf[java.util.Set[Object]].toList)
			violations = violations ++ List(violation)
			cycleDetector.findCycles
		}
		violations
	}
}
