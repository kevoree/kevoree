package org.kevoree.library.reasoner.ecj;

import ec.EvolutionState;
import ec.Individual;
import ec.Species;
import ec.util.Parameter;
import ec.vector.VectorDefaults;
import ec.vector.VectorIndividual;

public class KevoreeModelSpecies extends Species {

    public static final String P_KEVOREE_SPECIES = "species";
    
    public Parameter defaultBase() {
        return KevoreeDefaults.base().push(P_KEVOREE_SPECIES);
    }
    
    public Individual newIndividual(final EvolutionState state, int thread) 
    
    {
    KevoreeIndividual newind = (KevoreeIndividual)(super.newIndividual(state, thread));
    // TODO add mutation here ?           
    return newind;
    }

}
