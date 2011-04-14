package com.espertech.esper.epl.spec;

import com.espertech.esper.util.MetaDefItem;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Specification for property evaluation.
 */
public class PropertyEvalSpec implements MetaDefItem, Serializable
{
    private List<PropertyEvalAtom> atoms;
    private static final long serialVersionUID = -8843638696605082278L;

    /**
     * Ctor.
     */
    public PropertyEvalSpec()
    {
        this.atoms = new ArrayList<PropertyEvalAtom>();
    }

    /**
     * Return a list of atoms.
     * @return atoms
     */
    public List<PropertyEvalAtom> getAtoms()
    {
        return atoms;
    }

    /**
     * Add an atom.
     * @param atom to add
     */
    public void add(PropertyEvalAtom atom)
    {
        atoms.add(atom);
    }
}
