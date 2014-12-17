package org.kevoree.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/11/2013
 * Time: 00:16
 */
public interface Callback<T> {

    /**
     * @param result content of the answer (originChannelPath: name of the channel who forwarded the answer, originPortPath: path of the port who answered the call)
     */
    public void onSuccess(CallbackResult result);

    /**
     * @param exception
     */
    public void onError(Throwable exception);

}
