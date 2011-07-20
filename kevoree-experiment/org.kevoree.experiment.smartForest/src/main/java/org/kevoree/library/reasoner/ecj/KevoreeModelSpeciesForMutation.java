package org.kevoree.library.reasoner.ecj;

import ec.EvolutionState;
import ec.Individual;
import ec.Species;
import ec.util.Parameter;

public class KevoreeModelSpeciesForMutation extends Species {

    public static final String P_KEVOREE_SPECIES = "species";
    
    public Parameter defaultBase() {
        return KevoreeDefaults.base().push(P_KEVOREE_SPECIES);
    }
    
    public Individual newIndividual(final EvolutionState state, int thread)
    {
        KevoreeIndividual ind = (KevoreeIndividual)super.newIndividual(state, thread);
        return ind;
    }

}
