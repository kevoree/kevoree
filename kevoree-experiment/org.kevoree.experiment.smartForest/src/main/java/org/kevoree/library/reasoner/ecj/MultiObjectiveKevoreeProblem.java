package org.kevoree.library.reasoner.ecj;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import org.kevoree.*;

import java.util.ArrayList;
import java.util.List;

public class MultiObjectiveKevoreeProblem extends Problem implements SimpleProblemForm {
    public static final String tempSensor = "TempSensor";
    public static final String smokeSensor = "SmokeSensor";
    public static final String humiditySensor = "HumiditySensor";


    public void evaluate(EvolutionState state, Individual ind,
            int subpopulation, int threadnum) {
        if (ind.evaluated) return;

        if (!(ind instanceof KevoreeMultiIndividual))
            state.output.fatal("Whoa!  It's not a KevoreeIndividual!!!",null);
        KevoreeMultiIndividual ki = (KevoreeMultiIndividual)ind;
        
        
       // float[] newObjectives = {evaluateFunctionnality(ki, functionnalityValue, -functionnalityValue/4), evaluateCommunicationDelay(ki), evaluateUnusedHubs(ki), evaluateLoadBalancing(ki), evaluateUnusefullComponent(ki), evaluatearchitectureSize(ki)};
        float[] newObjectives = {evaluatePrecision(ki.myModel), evaluateCPUConsumption(ki.myModel)};
        setFitness(state, ki, newObjectives);
    }


    private float evaluateCPUConsumption (ContainerRoot myModel){
        double[] cpuConsumptions = new double[myModel.getNodes().size()];
        for (int i=0; i<cpuConsumptions.length; i++){
            int size = myModel.getNodes().get(i).getComponents().size();
            if (size > 0)
                cpuConsumptions[i] = 40 + size*20;
            else
                cpuConsumptions[i] = 0;
            if (cpuConsumptions[i] > 100)
                cpuConsumptions[i] = 100;
        }
        double result = 0;
        for (int i=0; i<cpuConsumptions.length; i++){
            result+=cpuConsumptions[i];
        }
        result = result / cpuConsumptions.length;
        return (float)(result);
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
    
    public static float evaluatePrecision(ContainerRoot myModel){
        double precision = 0.0;
        int size = myModel.getNodes().size();
        Precision[]precisionArray = new Precision[size];

        for (int i=0; i<myModel.getNodes().size();i++){
            precisionArray[i] = new Precision();
            ContainerNode myNode = myModel.getNodes().get(i);
            if (containsInstance(myNode, tempSensor)){
                precisionArray[i].setTempPrecision(100.0);
            }
            if (containsInstance(myNode, smokeSensor)){
                precisionArray[i].setSmokePrecision(100.0);
            }
            if (containsInstance(myNode, humiditySensor)){
                precisionArray[i].setHumidityPrecision(100.0);
            }
        }
        for (int k=0; k<myModel.getNodes().size(); k++){
            if (!precisionArray[k].isCompleted()){
                Integer[] neighbours = getNeighbours(k, size);
                for (int j=0;j<neighbours.length;j++){
                    if (containsInstance(myModel.getNodes().get(neighbours[j]), tempSensor)){
                        precisionArray[k].setTempPrecision(precisionArray[k].getTempPrecision()+(100.0/8.0));
                    }
                    if (containsInstance(myModel.getNodes().get(neighbours[j]), smokeSensor)){
                        precisionArray[k].setSmokePrecision(precisionArray[k].getSmokePrecision() + (100.0/8.0));
                    }
                    if (containsInstance(myModel.getNodes().get(neighbours[j]), humiditySensor)){
                        precisionArray[k].setHumidityPrecision(precisionArray[k].getHumidityPrecision()+ (100.0/8.0));
                    }
                }
            }
        }

        for (int k=0; k<precisionArray.length;k++){
            precision +=precisionArray[k].getHumidityPrecision()+ precisionArray[k].getTempPrecision() + precisionArray[k].getSmokePrecision();
        }
        precision = precision / (precisionArray.length *3);

        //return (float)(100-Math.floor(precision/10)*10);
        return (float)Math.floor(100-precision);

    }

    public static Integer[] getNeighbours(int indice, int size) {
        int width = (int)Math.sqrt((double)size);
        int i = indice/width;
        int j = indice - (i*width);
        int minI = Math.max(0,i-1);
        int maxI = Math.min(width-1, i+1);
        int minJ = Math.max(0,j-1);
        int maxJ = Math.min(width-1, j+1);
        List<Integer> myList = new ArrayList<Integer>();
        for (int k=minI; k<=maxI; k++){
            for (int l=minJ; l<=maxJ; l++){
                myList.add(k*width+l);
            }
        }
        return myList.toArray(new Integer[myList.size()]);

    }

    public static boolean containsInstance(ContainerNode myNode, String componentName){
        for (ComponentInstance ci:myNode.getComponents()){
            if (((NamedElement)ci.getTypeDefinition()).getName().equalsIgnoreCase(componentName)){
                return true;
            }
        }
        return false;
    }

}
