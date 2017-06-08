package org.kevoree.adaptation.cmds;

import org.kevoree.adaptation.AdaptationCommand;
import org.kevoree.adaptation.AdaptationType;
import org.kevoree.adaptation.KevoreeAdaptationException;
import org.kevoree.modeling.api.KMFContainer;

/**
 *
 * Created by leiko on 3/1/17.
 */
public class ExecFailCmd implements AdaptationCommand {

    @Override
    public void execute() throws KevoreeAdaptationException {
        throw new KevoreeAdaptationException("execute failed");
    }

    @Override
    public void undo() throws KevoreeAdaptationException {
        // this will never be called in this command be cause execute() always fails
    }

    @Override
    public AdaptationType getType() {
        return null;
    }

    @Override
    public KMFContainer getElement() {
        return null;
    }
}
