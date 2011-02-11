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
package org.kevoree.adaptation.deploy.osgi.scheduling;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.MBinding;
import org.kevoree.Port;
import org.kevoree.adaptation.deploy.osgi.command.LifeCycleCommand;

/**
 *
 * @author edaubert
 */
public class ChocoScheduling {

    private IntegerVariable[] variables;
    private boolean[][] potentialConstraints;

    public List<LifeCycleCommand> schedule(List<LifeCycleCommand> commands, boolean start) {
	if (commands.size() > 1) {
	System.out.println(System.currentTimeMillis());
	//if (commands.get(0) instanceof LifeCycleCommand) {
	    Solver solver = new CPSolver();
	    solver.read(buildModel((List<LifeCycleCommand>) commands, start));

	    //ChocoLogging.toVerbose();
	    // And after solver.solve()
	    //ChocoLogging.flushLogs();

	    Boolean solutionExists = solver.solve();
	    if (solutionExists == Boolean.TRUE) {
		return sortCommands((List<LifeCycleCommand>) commands, solver);
	    } else if (solutionExists == Boolean.FALSE) {
		// TODO return somthing to tell that scheduling is not possible
	    } else {
		// TODO return somthing to tell that solver has reach timeout
	    }
	    // FIXME must never appears
	//}
	}
	return commands;
    }

    private Model buildModel(List<LifeCycleCommand> commands, boolean start) {
	variables = new IntegerVariable[commands.size()];
	Model model = new CPModel();
	int i = 0;

	// look for potential constraints
	lookForPotentialConstraints(commands);


	for (LifeCycleCommand command : commands) {
	    IntegerVariable variable = new IntegerVariable("v" + i, 0, commands.size());
	    variables[i] = variable;
	    model.addVariable(variable);

	    i++;
	}
	for (i = 0; i < commands.size(); i++) {
	    for (int j = 0; j < commands.size(); j++)  { // TODO void dernière boucle dans lookForConstraints
		if (potentialConstraints[i][j]) {
		    Constraint constraint1 = Choco.neq(variables[i], variables[j]);
		    model.addConstraint(constraint1);
		    if (start) {
			constraint1 = Choco.leq(variables[j], variables[i]);
		    } else {
			constraint1 = Choco.leq(variables[i], variables[j]);
		    }
		    model.addConstraint(constraint1);
		}
	    }
	}
	return model;
    }

    private List<LifeCycleCommand> sortCommands(List<LifeCycleCommand> commands, Solver solver) {
	List<LifeCycleCommand> sortedCommands = new ArrayList<LifeCycleCommand>(commands.size());
	int i = 0;
	for (LifeCycleCommand command : commands) {
	    sortedCommands.add(solver.getVar(variables[i]).getVal(), command);
	    //sortedCommands[solver.getVar(variables[i]).getVal()] = command;
	    i++;
	}
	System.out.println(System.currentTimeMillis());
	return sortedCommands;
    }

    private void lookForPotentialConstraints(List<LifeCycleCommand> commands) {
	// instance is a dependency of all instances contained on the list
	Map<Instance, List<Instance>> instanceDependencies = new HashMap<Instance, List<Instance>>();
	//if (commands.get(0) instanceof LifeCycleCommand) {
	    for (MBinding binding : ((ContainerRoot) ((LifeCycleCommand) commands.get(0)).getInstance().eContainer()).getMBindings()) {
		for (LifeCycleCommand command : (List<LifeCycleCommand>) commands) {
		    if (command.getInstance() instanceof ComponentInstance) {
			ComponentInstance instance = (ComponentInstance) command.getInstance();
			// test all provided port
			// the instance of the provided port must be stopped before those which are connected to him
			for (Port port : instance.getProvided()) {
			    if (binding.getPort().equals(port)) {
				List<Instance> list = instanceDependencies.get(instance);
				if (list == null) {
				    list = new ArrayList<Instance>();
				}
				list.add(binding.getHub());
				instanceDependencies.put(instance, list);
			    }
			}
			// test all required port
			// the instance wait stops of all of those which are connected to him
			for (Port port : instance.getRequired()) {
			    if (binding.getPort().equals(port)) {
				List<Instance> list = instanceDependencies.get(binding.getHub());
				if (list == null) {
				    list = new ArrayList<Instance>();
				}
				list.add(instance);
				instanceDependencies.put(binding.getHub(), list);
			    }
			}
		    } else if (command.getInstance() instanceof Channel) {
			// FIXME maybe not needed because all cases are managed using only ComponentInstances
		    }
		}
	    }
	    potentialConstraints = new boolean[commands.size()][commands.size()];
	    int i = 0;
	    for (LifeCycleCommand command : (List<LifeCycleCommand>) commands) {
		int j = 0;
		for (LifeCycleCommand command2 : (List<LifeCycleCommand>) commands) {

		    if (instanceDependencies.get(command2.getInstance()) != null && instanceDependencies.get(command.getInstance()) != null && instanceDependencies.get(command2.getInstance()).contains(command.getInstance())) {
			potentialConstraints[j][i] = true;
		    }
		    j++;
		}
		i++;
	    }
	//}
    }
}
