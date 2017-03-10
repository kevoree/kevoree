package org.kevoree.adaptation;

/**
 *
 */
public interface AdaptationCommand {

    void execute() throws KevoreeAdaptationException;

    void undo() throws KevoreeAdaptationException;

    AdaptationType getType();
}
