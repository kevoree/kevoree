package org.kevoree.library.reasoner.ecj;

import ec.DefaultsForm;
import ec.util.Parameter;

public class KevoreeDefaults implements DefaultsForm {
    public static final String P_Kevoree = "kevoree";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_Kevoree);
        } 
}
