package org.kevoree.adaptation;

import org.kevoree.modeling.api.KMFContainer;

/**
 *
 */
public interface AdaptationCommand {

    void execute() throws KevoreeAdaptationException;

    void undo() throws KevoreeAdaptationException;

    AdaptationType getType();

    KMFContainer getElement();
}
