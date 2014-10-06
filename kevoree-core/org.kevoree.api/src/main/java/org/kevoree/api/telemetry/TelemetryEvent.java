package org.kevoree.api.telemetry;

/**
 * Created by duke on 8/7/14.
 */
public interface TelemetryEvent {

    public enum Type {LOG_INFO, LOG_WARNING, LOG_DEBUG, LOG_ERROR, LOG_TRACE, PLATFORM_START, PLATFORM_STOP, PLATFORM_UPDATE_START, PLATFORM_UPDATE_SUCCESS, PLATFORM_UPDATE_FAIL, MODEL_COMPARE_AND_PLAN, DEPLOYMENT_STEP, SYSTEM_INFO}

    public Type type();

    public Long timestamp();

    public String origin();

    public String message();

    public String stack();

    public String toJSON();

}
