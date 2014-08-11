package org.kevoree.api.telemetry;

/**
 * Created by duke on 8/7/14.
 */
public interface TelemetryEvent {

    public String origin();

    public String message();

    public String stack();

    public String toJSON();

}
