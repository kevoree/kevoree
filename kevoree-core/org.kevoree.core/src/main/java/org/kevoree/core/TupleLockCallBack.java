package org.kevoree.core;

import org.kevoree.api.handler.LockCallBack;

import java.util.UUID;

/**
 * Created by duke on 9/26/14.
 */
public class TupleLockCallBack {

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;

    public TupleLockCallBack(UUID uuid, LockCallBack callback) {
        this.uuid = uuid;
        this.callback = callback;
    }

    public LockCallBack getCallback() {
        return callback;
    }

    public void setCallback(LockCallBack callback) {
        this.callback = callback;
    }

    private LockCallBack callback;

}
