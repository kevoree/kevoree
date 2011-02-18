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

package org.kevoree.core.basechecker

/*import choco.cp.model.CPModel
 import choco.cp.solver.CPSolver
 import choco.kernel.model.Model
 import choco.kernel.model.variables.integer.IntegerVariable
 import choco.kernel.solver.Solver*/
import org.jgrapht.alg.CycleDetector
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.MBinding
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import scala.collection.JavaConversions._

class CycleChecker extends CheckerService {

  def check(model:ContainerRoot ):java.util.List[CheckerViolation] = {
    var violations : List[CheckerViolation] = List()
    model.getNodes.foreach{
      node =>
      var graph  = KevoreeDirectedGraph(model,node.getName)
      var cycleDetector  = new CycleDetector[Instance, MBinding](graph)
      if (cycleDetector.detectCycles) {
	var violation = new CheckerViolation
	violation.setMessage("Cycle(s) detected")
	violations = violations ++ List(violation)
	cycleDetector.findCycles
      }
    }

    /*var graph = KevoreeDirectedGraph(model,)

     var cycleDetector : CycleDetector = new CycleDetector(KevoreeDirectedGraph())*/

    return violations

  }

  /*private def checkCyclicDependencies(model:ContainerRoot ):java.util.List[CheckerViolation] = {

   // for each bindings, we define variables and constraints about dependencies between the two instances used by the binding
   val solver: Solver = new CPSolver
   solver.read(buildModelForCyclicDependenciesCheck(model))
   var solutionExists = solver.isFeasible
   if (solutionExists == false) {
   return null
   } else {
   return null
   }
   }
   private def buildModelForCyclicDependenciesCheck(model : ContainerRoot) : Model ={
   val chocoModel : Model = new CPModel

   var variables: Map[Instance,IntegerVariable] = Map()
    
   var bindingIterator = model.getMBindings.iterator
   while(bindingIterator.hasNext) {
   var binding = bindingIterator.next

   var instance : ComponentInstance = binding.getPort.eContainer.asInstanceOf[ComponentInstance]

   /*val variable: IntegerVariable = new IntegerVariable(instance.getName, 0, (commands.size-1))
    if (instance.getProvided.contains(binding.getPort)) {

    } else {

    }*/




      
   //val variable: IntegerVariable = new IntegerVariable("v" + i, 0, (commands.size-1))
   //variables = variables + (command -> variable)
   //model.addVariable(variable)
   //i += 1
   }
   return null
   }

   /*
    * Return Map
    *
    * Instance Map key is a dependency of all List instances return by key
    *
    *
    * [i:Instance,li:List[Instance]]
    * li depends i
    *
    *
    * */
   private def lookForPotentialConstraints(model : ContainerRoot): scala.collection.mutable.Map[Instance, java.util.List[Instance]] = {
   val instanceDependencies: scala.collection.mutable.Map[Instance, java.util.List[Instance]] = scala.collection.mutable.Map[Instance, java.util.List[Instance]]()




   var bindingIterator = model.getMBindings.iterator
   while(bindingIterator.hasNext){
   var binding = bindingIterator.next
   for (command <- commands) {
   command.getInstance match {
   case instance: ComponentInstance => {
   // test all provided port
   // the instance of the provided port must be stopped before those which are connected to him
   var pit = instance.getProvided.iterator
   while(pit.hasNext){
   var port = pit.next
   if (binding.getPort.equals(port)) {
   var newL = instanceDependencies.get(instance).getOrElse(new java.util.ArrayList())
   newL.add(binding.getHub)
   instanceDependencies.update(instance, newL)
   }
   }
   // test all required port
   // the instance wait stops of all of those which are connected to him
   var rit = instance.getRequired.iterator
   while(rit.hasNext){
   var port = rit.next
   if (binding.getPort.equals(port)) {
   var newL = instanceDependencies.get(binding.getHub).getOrElse(new java.util.ArrayList())
   newL.add(instance)
   instanceDependencies.update(binding.getHub, newL)
   }
   }
   }
   case _ =>
   }
   }
   }
   instanceDependencies
   }*/

}
