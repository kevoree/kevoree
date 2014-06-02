package org.kevoree.api.handler;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/12/13
 * Time: 15:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class ModelListenerAdapter implements ModelListener {

    @Override
    public boolean preUpdate(UpdateContext context) {
        return true;
    }

    @Override
    public boolean initUpdate(UpdateContext context) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(UpdateContext context) {
        return true;
    }

    @Override
    public void preRollback(UpdateContext context) {

    }

    @Override
    public void postRollback(UpdateContext context) {

    }
}
