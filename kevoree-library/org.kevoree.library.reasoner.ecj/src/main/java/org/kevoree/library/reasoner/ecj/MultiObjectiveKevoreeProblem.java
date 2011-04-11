package org.kevoree.library.reasoner.ecj;

import java.util.ArrayList;

import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.MBinding;
import org.kevoree.Port;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;

public class MultiObjectiveKevoreeProblem extends Problem implements SimpleProblemForm {
    public static final float functionnalityValue = 20;

    public void evaluate(EvolutionState state, Individual ind,
            int subpopulation, int threadnum) {
        if (ind.evaluated) return;

        if (!(ind instanceof KevoreeIndividual))
            state.output.fatal("Whoa!  It's not a KevoreeIndividual!!!",null);
        KevoreeIndividual ki = (KevoreeIndividual)ind;    
        
        
        float[] newObjectives = {evaluateFunctionnality(ki, functionnalityValue, -functionnalityValue/4), evaluateCommunicationDelay(ki), evaluateUnusedHubs(ki), evaluateLoadBalancing(ki), evaluateUnusefullComponent(ki), evaluatearchitectureSize(ki)};
        setFitness(state, ki, newObjectives);
    }

 // Will give a good score to architecture where all components are useful
    private float evaluateUnusedHubs(KevoreeIndividual ki) {
        float fitness = 100;
        float ununsedHubs = 0;
        float totalHubs = ki.myModel.getHubs().size();
        for (Channel myChannel : ki.myModel.getHubs()) {
            boolean contradictoryBinding = false;
            boolean findOneBinding = false, isRequired = false;
            for (MBinding myBinding : ki.myModel.getMBindings()) {
                if (!findOneBinding && myBinding.getHub().equals(myChannel)){
                    findOneBinding = true;
                    if (isRequired(myBinding.getPort())){
                        isRequired = true;
                    } else {
                        isRequired = false;
                    }
                    break;
                } else
                    if (findOneBinding && isRequired && !isRequired(myBinding.getPort())){
                        contradictoryBinding = true;
                    } else if (findOneBinding && !isRequired && isRequired(myBinding.getPort())){
                        contradictoryBinding = true;
                    } 
                        
            }
            if (!findOneBinding || (findOneBinding && !contradictoryBinding)){
                ununsedHubs++;
            }
        }
        fitness = ununsedHubs;
        return fitness;
    }
    
 // Will give a good score to small architecture
    private float evaluatearchitectureSize(KevoreeIndividual ki) {
        float fitness = 100;
        float totalHubs = ki.myModel.getHubs().size();
        float totalComponents = 0;
        for (ContainerNode myNode : ki.myModel.getNodes()) {
            totalComponents += myNode.getComponents().size();
        }
        float totalBindings = ki.myModel.getMBindings().size();
        
        fitness = totalHubs + totalComponents + totalBindings;
        return fitness;
    }
    
    // Will give a good score to architecture with few communications between components present on distributed nodes
    private float evaluateCommunicationDelay(KevoreeIndividual ki) {
        float numberOfInterNodesBindings = 0;
        float totalNumberOfBindings = ki.myModel.getMBindings().size();
        if (totalNumberOfBindings <= 1){
            return 0;
        }
        for (MBinding myBinding : ki.myModel.getMBindings()) {
            for (MBinding myBinding2 : ki.myModel.getMBindings()) {
                if ((!myBinding.equals(myBinding2) && myBinding.getHub().equals(myBinding2.getHub()) && ((isRequired(myBinding.getPort()) && !isRequired(myBinding2.getPort())) || (!isRequired(myBinding.getPort()) && isRequired(myBinding2.getPort()))))){
                    // test if the component  
                    if (!myBinding.getPort().eContainer().eContainer().equals(myBinding2.getPort().eContainer().eContainer())){
                        numberOfInterNodesBindings++;
                    }
                }
            }
        }
        numberOfInterNodesBindings = numberOfInterNodesBindings/2;
        if (numberOfInterNodesBindings == 0){
            return 0;
        }
        
        return numberOfInterNodesBindings;
    }
    
    // return true if this port is a required port, false if it is a provided port.
    private boolean isRequired(Port port) {
        for (Port requiredPort : ((ComponentInstance)port.eContainer()).getRequired()) {
            if (requiredPort.equals(port)){
                return true;
            }
        }
        return false;
    }

    // Will give a good score to architecture which emphasize load balancing on every available components
    private float evaluateLoadBalancing(KevoreeIndividual ki) {
        if (ki.myModel.getNodes().size() > 0) {
            int[] myNodes = new int[ki.myModel.getNodes().size()];
            int i = 0;
            for (ContainerNode myNode : ki.myModel.getNodes()) {
                myNodes[i] = myNode.getComponents().size();
                i++;
            }
            int min = myNodes[0];
            int max = myNodes[0];
            for (int numberOfComponents : myNodes) {
                if (min > numberOfComponents) {
                    min = numberOfComponents;
                }
                if (max < numberOfComponents) {
                    max = numberOfComponents;
                }
            }
            if (max > 0) {
                return max - min;
            } else {
                return 0;
            }
        }
        return 0;
    }
    
    // Will give a good score to architecture where all components are useful
    private float evaluateUnusefullComponent(KevoreeIndividual ki) {
        float fitness = 100;
        float ununsedComponents = 0;
        float totalComponents = 0;
        for (ContainerNode myNode : ki.myModel.getNodes()) {
            for (ComponentInstance myComponent : myNode.getComponents()) {
                totalComponents ++;
                boolean findOneBinding = false;
                for (MBinding myBinding : ki.myModel.getMBindings()) {
                    if (myBinding.getPort().eContainer().equals(myComponent)){
                        findOneBinding = true;
                        break;
                    }
                }
                if (!findOneBinding){
                    ununsedComponents++;
                }
            }
        }
        fitness = ununsedComponents;
        return fitness;
    }

    private void setFitness(EvolutionState state, KevoreeIndividual ki, float[] newObjectives) {
        if (!(ki.fitness instanceof MultiObjectiveFitness)) {
            state.output.fatal("Whoa!  It's not a MultiObjective!!!", null);
        }
        ((MultiObjectiveFitness) ki.fitness).setObjectives(state, newObjectives); // the
                                                                    // fitness
                                                                    // is ideal
                                                                    // if the
                                                                    // fitness
                                                                    // is
                                                                    // superior
                                                                    // or equal
                                                                    // to 99 in
                                                                    // this
                                                                    // problem
        ki.evaluated = true;
    }
    
    public static float evaluateFunctionnality(KevoreeIndividual ki, float functionnalityValue, float increment){
        float fitness = functionnalityValue;
        boolean onToOnBinding = false, onToShowTextBinding = false, offToOffBinding = false, toggleToToggleBinding = false;
        for (MBinding myBinding : ki.myModel.getMBindings()) {
            if (!onToOnBinding && myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!onToOnBinding && !myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness+=increment;
                            onToOnBinding = true;
                        }
                    }
                }
            }
            if (!onToShowTextBinding && myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!onToShowTextBinding && !myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("showText") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeConsole")){
                            fitness+=increment;
                            onToShowTextBinding = true;
                        }
                    }
                }
            }
            if (!offToOffBinding && myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!offToOffBinding && !myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness+=increment;
                            offToOffBinding = true;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness = functionnalityValue;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness = functionnalityValue;
                            break;
                        }
                    }
                }
            }
            if (!toggleToToggleBinding && myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!toggleToToggleBinding && !myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness+=increment;
                            toggleToToggleBinding = true;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness = functionnalityValue;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness = functionnalityValue;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness = functionnalityValue;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            fitness = functionnalityValue;
                            break;
                        }
                    }
                }
            }
        }
        return fitness;
    }

}
