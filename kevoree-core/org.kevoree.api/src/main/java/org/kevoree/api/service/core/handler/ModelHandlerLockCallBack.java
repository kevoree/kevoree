package org.kevoree.api.service.core.handler;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/02/12
 * Time: 14:05
 */
public interface ModelHandlerLockCallBack {

    public void lockTimeout();

    public void lockRejected();

    public void lockAcquired(UUID bypassUUID);

}
