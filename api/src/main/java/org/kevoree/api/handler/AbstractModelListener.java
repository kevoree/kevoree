package org.kevoree.api.handler;

import org.kevoree.adaptation.KevoreeAdaptationException;

/**
 * Helper class that gives default implementation for ModelListener
 * preUpdate() and preRollback() return "true" by default
 *
 * Created by leiko on 3/1/17.
 */
public abstract class AbstractModelListener implements ModelListener {

    @Override
    public boolean preUpdate(final UpdateContext context) {
        return true;
    }

    @Override
    public void updateSuccess(final UpdateContext context) {}

    @Override
    public void updateError(final UpdateContext context, final KevoreeAdaptationException error) {}

    @Override
    public boolean preRollback(final UpdateContext context) {
        return true;
    }

    @Override
    public void rollbackSuccess(final UpdateContext context) {}

    @Override
    public void rollbackError(final UpdateContext context, final KevoreeAdaptationException error) {}

    @Override
    public void preUpdateRefused(final UpdateContext context) {}

    @Override
    public void preRollbackRefused(final UpdateContext context) {}
}
