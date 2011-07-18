package org.kevoree.library.reasoner.ecj;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;

public class SimpleKevoreeProblem extends Problem implements SimpleProblemForm {

    public void evaluate(EvolutionState state, Individual ind,
            int subpopulation, int threadnum) {
        if (ind.evaluated) return;

        if (!(ind instanceof KevoreeIndividual))
            state.output.fatal("Whoa!  It's not a KevoreeIndividual!!!",null);
        KevoreeIndividual ki = (KevoreeIndividual)ind;
//        float fitness = MultiObjectiveKevoreeProblem.evaluateFunctionnality(ki, 0, 25);
        float fitness = 0;
        Float f = new Float(fitness);
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
