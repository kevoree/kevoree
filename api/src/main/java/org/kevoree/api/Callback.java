package org.kevoree.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/11/2013
 * Time: 00:16
 */
public interface Callback {

    /**
     * @param result content of the answer (originChannelPath: name of the channel who forwarded the answer, originPortPath: path of the port who answered the call)
     */
    void onSuccess(CallbackResult result);

    /**
     * @param exception
     */
    void onError(Throwable exception);

}
