package org.kevoree.api.handler;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/11/12
 * Time: 10:06
 */
public interface UpdateCallback {

    /**
     * Called when model is updated
     *
     * @param e if set then something went wrong; null if deployed successfully
     */
    void run(Exception e);
}
