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
package org.kevoree.adaptation.deploy.osgi.scheduling

import choco.Choco
import choco.cp.model.CPModel
import choco.cp.solver.CPSolver
import choco.kernel.model.Model
import choco.kernel.model.constraints.Constraint
import choco.kernel.model.variables.integer.IntegerVariable
import choco.kernel.solver.Solver
import org.kevoree.Channel
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.MBinding
import org.kevoree.Port
import org.kevoree.adaptation.deploy.osgi.command.LifeCycleCommand
//import scala.collection.JavaConversions._

/**
 *
 * @author edaubert
 *
 * Mapping command to Integer value
 *
 * Semantic : if Action1 < Action2
 *        Action1 must be executed before Action2
 *
 *
 */
class ChocoScheduling {

  def schedule(commands: List[LifeCycleCommand], start: Boolean): List[LifeCycleCommand] = {
    if (commands.size > 1) {
      val solver: Solver = new CPSolver
      solver.read(buildModel(commands, start))
      var solutionExists = solver.solve
      if (solutionExists == true) {
        return sortCommands(commands, solver).toList
      }
      else if (solutionExists == false) {
        System.err.println("Plannification found no solution , circular dependencies ?");
        // TODO return somthing to tell that scheduling is not possible
      }
      else {
        // TODO return somthing to tell that solver has reach timeout
      }
    }
    return commands
  }

  private def buildModel(commands: List[LifeCycleCommand], start: Boolean): Model = {
    val model: Model = new CPModel
    var map = lookForPotentialConstraints(commands)

    var i: Int = 0
    commands.foreach({
        command =>
        val variable: IntegerVariable = new IntegerVariable("v" + i, 0, (commands.size-1))
        variables = variables + (command -> variable)
        model.addVariable(variable)
        i += 1
      })

    for (command <- commands) {      
      for (command2 <- commands) {            
        if (!command.equals(command2)) {
          (map.get(command2.getInstance)) match {
            case Some(cmdDep) => {
                var constraint1: Constraint = null;//Choco.neq(variables.get(command).get, variables.get(command2).get)
                //model.addConstraint(constraint1)

                //println(command.getInstance.getName + "!="+command2.getInstance.getName)

                if (cmdDep.contains(command.getInstance)){
                  if (start) {
                    constraint1 = Choco.lt(variables.get(command2).get, variables.get(command).get)
                    //println(command2.getInstance.getName+"<"+command.getInstance.getName)
                  }
                  else {
                    constraint1 = Choco.lt(variables.get(command).get, variables.get(command2).get)
                  }
                  
                } else {
                  //println(command.getInstance.getName + "!="+command2.getInstance.getName)
                  constraint1 = Choco.neq(variables.get(command).get, variables.get(command2).get)
                }
                model.addConstraint(constraint1)
              }
            case _ => {
                //println(command.getInstance.getName + "!="+command2.getInstance.getName)
                var constraint1: Constraint = Choco.neq(variables.get(command).get, variables.get(command2).get)
                model.addConstraint(constraint1)
              }
          }
        }
            
      }
    }
    return model
  }

  private def sortCommands(commands: List[LifeCycleCommand], solver: Solver): Array[LifeCycleCommand] = {
    val sortedCommands = new Array[LifeCycleCommand](commands.size)
    for (command <- commands) {
      var index = solver.getIntVarIndex(solver.getVar(variables.get(command).get))
      // println("res="+command.getInstance.getName+"-"+solver.getIntVar(index).getVal)

      
      sortedCommands.update(solver.getIntVar(index).getVal, command)
    }
    return sortedCommands
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
  private def lookForPotentialConstraints(commands: List[LifeCycleCommand]): scala.collection.mutable.Map[Instance, java.util.List[Instance]] = {
    val instanceDependencies: scala.collection.mutable.Map[Instance, java.util.List[Instance]] = scala.collection.mutable.Map[Instance, java.util.List[Instance]]()

    var rootContainer : ContainerRoot = null
    var firstCommand= (commands(0)).getInstance
    firstCommand match {
      case c : Channel => rootContainer = c.eContainer.asInstanceOf[ContainerRoot]
      case c : ComponentInstance => c.eContainer.eContainer.asInstanceOf[ContainerRoot]
    }



    var bindingIterator = rootContainer.getMBindings.iterator
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
  }

  private var variables: Map[LifeCycleCommand,IntegerVariable] = Map()
  private var potentialConstraints: Array[Array[Boolean]] = null
}