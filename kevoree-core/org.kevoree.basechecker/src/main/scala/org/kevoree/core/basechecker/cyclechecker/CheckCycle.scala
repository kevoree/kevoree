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
package org.kevoree.core.basechecker.cyclechecker

import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.kevoree.api.service.core.checker.CheckerViolation

case class CheckCycle[A,B](graph: DirectedGraph[A, B]) {
	
	def check(): List[CheckerViolation] = {
		var violations: List[CheckerViolation] = List()
		val cycleDetector = new CycleDetector[A, B](graph)

		if (cycleDetector.detectCycles) {
			val violation = new CheckerViolation
			violation.setMessage("Cycle(s) detected")
      import scala.collection.JavaConversions._
			violation.setTargetObjects(cycleDetector.findCycles.asInstanceOf[java.util.Set[Object]].toList)
			violations = violations ++ List(violation)
			//cycleDetector.findCycles
		}
		violations // TODO convert ChannelFragment by Channel before returning CheckerViolation
	}
}