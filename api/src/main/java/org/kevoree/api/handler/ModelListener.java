package org.kevoree.api.handler;


import org.kevoree.adaptation.KevoreeAdaptationException;

public interface ModelListener {

    /**
     * Method called before Kevoree KevoreeCore accept an input model
     * If one of the listeners returns "false" to this method, then no update
     * will occur
     *
     * @param context the associated update context
     * @return true if ok to update; false otherwise
     */
    boolean preUpdate(UpdateContext context);

    /**
     * Method called after a model is successfully deployed
     *
     * @param context the associated update context
     */
    void updateSuccess(UpdateContext context);

    /**
     * Method called after something went wrong with a deployment
     *
     * @param context the associated update context
     * @param error supposed to tell what went wrong
     */
    void updateError(UpdateContext context, KevoreeAdaptationException error);

    /**
     * Method called before Kevoree KevoreeCore starts the rollback phase
     * If one of the listeners returns "false" to this method, then no rollback
     * will occur
     *
     * @param context the associated update context
     * @return true if ok to rollback; false otherwise
     */
    boolean preRollback(UpdateContext context);

    /**
     * Method called after a successful rollback occurred
     *
     * @param context the associated update context
     */
    void rollbackSuccess(UpdateContext context);

    /**
     * Method called after an error during rollback phase
     *
     * @param context the associated update context
     * @param error what went wrong during rollback
     */
    void rollbackError(UpdateContext context, KevoreeAdaptationException error);

    /**
     * Method called when one of the listeners refused to proceed update
     *
     * @param context the associated update context
     */
    void preUpdateRefused(UpdateContext context);

    /**
     * Method called when one of the listeners refused to proceed rollback
     *
     * @param context the associated update context
     */
    void preRollbackRefused(UpdateContext context);
}
