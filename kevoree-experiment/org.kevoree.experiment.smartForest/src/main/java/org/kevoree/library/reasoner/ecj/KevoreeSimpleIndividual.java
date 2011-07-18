package org.kevoree.library.reasoner.ecj;

import org.kevoree.library.reasoner.ecj.dpa.*;
import org.kevoree.library.tools.dpa.DPA;

public class KevoreeSimpleIndividual extends KevoreeIndividual {

    public final static DPA[] SimpleDpas = {
            new AddChannelDPA(),
            new AddComponentDPA(),
            new AddBindingDPA(),
            new RemoveBindingDPA()
    };

     public Object clone() {
        KevoreeSimpleIndividual ki = (KevoreeSimpleIndividual) super.clone();
        ki.dpas = SimpleDpas;
        return ki;
    }
}
