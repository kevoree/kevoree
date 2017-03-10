package org.kevoree.api;

/**
 *
 * Created by duke on 17/12/14.
 */
public class CallbackResult {

    private String originPortPath;

    private String originChannelPath;

    private String payload;

    public String getOriginPortPath() {
        return originPortPath;
    }

    public void setOriginPortPath(String originPortPath) {
        this.originPortPath = originPortPath;
    }

    public String getOriginChannelPath() {
        return originChannelPath;
    }

    public void setOriginChannelPath(String originChannelPath) {
        this.originChannelPath = originChannelPath;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
    
}
