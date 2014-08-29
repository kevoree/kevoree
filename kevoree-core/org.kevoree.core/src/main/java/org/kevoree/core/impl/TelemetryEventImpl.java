package org.kevoree.core.impl;

import org.kevoree.api.telemetry.TelemetryEvent;

/**
 * Created by duke on 8/7/14.
 */
public class TelemetryEventImpl implements TelemetryEvent {

    private String origin;
    private String message;
    private String type;
    private String stack;
    private Long timestamp;

    public static TelemetryEvent build(String origin, String type, String message, String stack) {
        TelemetryEventImpl e = new TelemetryEventImpl();
        e.origin = origin;
        e.type = type;
        e.message = message;
        e.stack = stack;
        e.timestamp = System.nanoTime() / 1000;
        return e;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Long timestamp() {
        return timestamp;
    }

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String stack() {
        return stack;
    }

    @Override
    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("\"origin\":\"" + origin + "\",\n");
        builder.append("\"type\":\"" + type + "\",\n");
        builder.append("\"message\":\"" + message.replace("\n", "\\n").replace("\t", "\\t") + "\",\n");
        builder.append("\"timestamp\":\"" + timestamp + "\",\n");
        builder.append("\"stack\":\"" + stack.replace("\n", "\\n").replace("\t", "\\t") + "\"\n");
        builder.append("}\n");
        return builder.toString();
    }
}
