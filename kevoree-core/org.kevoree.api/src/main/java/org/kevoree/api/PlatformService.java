package org.kevoree.api;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.LockCallBack;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.telemetry.TelemetryListener;
import org.kevoree.pmodeling.api.trace.TraceSequence;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 17:25
 */
public interface PlatformService {

    void addTelemetryListener(TelemetryListener tl);
    void removeTelemetryListener(TelemetryListener tl);

}
