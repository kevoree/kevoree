package org.kevoree.api.telemetry;

/**
 * Created by duke on 8/7/14.
 */
public interface TelemetryListener {

    public void notify(TelemetryEvent event);

}
