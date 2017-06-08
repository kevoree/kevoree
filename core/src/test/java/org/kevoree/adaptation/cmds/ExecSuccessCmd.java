package org.kevoree.adaptation.cmds;

import org.kevoree.adaptation.AdaptationCommand;
import org.kevoree.adaptation.AdaptationType;
import org.kevoree.adaptation.KevoreeAdaptationException;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;

/**
 *
 * Created by leiko on 3/1/17.
 */
public class ExecSuccessCmd implements AdaptationCommand {
    
    @Override
    public void execute() throws KevoreeAdaptationException {
        Log.info("{} executed successfully", this);
    }

    @Override
    public void undo() throws KevoreeAdaptationException {
        Log.info("{} undo-ed successfully", this);
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
