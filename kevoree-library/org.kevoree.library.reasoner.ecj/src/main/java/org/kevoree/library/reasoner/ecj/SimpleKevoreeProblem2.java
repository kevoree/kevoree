package org.kevoree.library.reasoner.ecj;

import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.MBinding;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;

public class SimpleKevoreeProblem2 extends Problem implements SimpleProblemForm {

    public void evaluate(EvolutionState state, Individual ind,
            int subpopulation, int threadnum) {
        if (ind.evaluated) return;

        if (!(ind instanceof KevoreeIndividual))
            state.output.fatal("Whoa!  It's not a KevoreeIndividual!!!",null);
        KevoreeIndividual ki = (KevoreeIndividual)ind;
        if (ki.myModel.getNodes().size()!= 1){
            setFitness(state, ki, 0);
            return;
        }
        //components evaluation
        ContainerNode cn = ki.myModel.getNodes().get(0);
        
        if (cn.getComponents().size()>3 || ki.myModel.getHubs().size()>3){
            setFitness(state, ki, 0);
            return;
        }
        double componentNumberMultiplier = 0.0 ;
        if (cn.getComponents().size()<=3) {
            componentNumberMultiplier = cn.getComponents().size() * 33.3;
        }
        double componentTypeMultiplier = 40.0;
        boolean fakeLight = false, fakeSwitch = false, fakeConsole = false;
        for (ComponentInstance myComp : cn.getComponents()) {
            if (myComp.getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") && !fakeLight){
                componentTypeMultiplier += 20;
                fakeLight = true;
            }
            if (myComp.getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch") && !fakeSwitch){
                componentTypeMultiplier += 20;
                fakeSwitch = true;
            }
            if (myComp.getTypeDefinition().getName().equalsIgnoreCase("FakeConsole") && !fakeConsole){
                componentTypeMultiplier += 20;
                fakeConsole = true;
            }
        }
        // channels evaluation
        double channelNumberMultiplier = 0 ;
        if (ki.myModel.getHubs().size()<=3) {
            channelNumberMultiplier = 10 + (ki.myModel.getHubs().size() * 30);
        }
        double channelTypeMultiplier = 40.0;
        for (Channel myChannel : ki.myModel.getHubs()) {
            if (myChannel.getTypeDefinition().getName().equalsIgnoreCase("defMSG")){
                if (channelTypeMultiplier+20 <=100)
                        channelTypeMultiplier += 20;
            }
        }
        
        //bindings evaluation
        double bindingNumberMultiplier = 100 ;
//        if (ki.myModel.getMBindings().size()>=5) {
//            bindingNumberMultiplier = 100 - ((ki.myModel.getMBindings().size()-5) * 5);
//            if (bindingNumberMultiplier<0){
//                bindingNumberMultiplier = 0;
//            }
//        }
        
        double bindingTypeMultiplier = 20.0;
        boolean onToOnBinding = false, onToShowTextBinding = false, offToOffBinding = false, toggleToToggleBinding = false;
        for (MBinding myBinding : ki.myModel.getMBindings()) {
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") &&  !onToOnBinding){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier += 20;
                            onToOnBinding = true;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch") && !onToShowTextBinding){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("showText") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeConsole")){
                            bindingTypeMultiplier += 20;
                            onToShowTextBinding = true;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") && !offToOffBinding){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier += 20;
                            offToOffBinding = true;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier = 0;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier = 0;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") &&  !toggleToToggleBinding){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier += 20;
                            toggleToToggleBinding = true;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier = 0;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier = 0;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier = 0;
                            break;
                        }
                    }
                }
            }
            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("toggle") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
                for (MBinding myBinding1 : ki.myModel.getMBindings()){
                    if (!myBinding.equals(myBinding1)){
                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
                            bindingTypeMultiplier = 0;
                            break;
                        }
                    }
                }
            }
        }
        
        double fitness = 50.0*(componentNumberMultiplier/100.0)*(componentTypeMultiplier/100.0);
        fitness += 30.0*(channelNumberMultiplier/100.0)*(channelTypeMultiplier/100.0);
        fitness += 20.0*(bindingNumberMultiplier/100.0)*(bindingTypeMultiplier/100.0);
        Double f = new Double(fitness);
        setFitness(state, ki, f.intValue());
    }

    private void setFitness(EvolutionState state, KevoreeIndividual ki, int i) {
        if (!(ki.fitness instanceof SimpleFitness)) {
            state.output.fatal("Whoa!  It's not a SimpleFitness!!!", null);
        }
        ((SimpleFitness) ki.fitness).setFitness(state, i, i >= 99); // the
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

}
