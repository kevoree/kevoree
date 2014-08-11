package org.kevoree.platform.standalone.telemetry;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.api.telemetry.TelemetryListener;
import org.kevoree.log.Log;

import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by duke on 8/7/14.
 */
public class MQTTDispatcher implements TelemetryListener, Listener, Runnable {

    String url;

    String nodeName;

    private CallbackConnection connection;

    private ScheduledExecutorService restartPool = Executors.newScheduledThreadPool(1);


    public MQTTDispatcher(String u, String nn) throws URISyntaxException {
        this.url = u;
        this.nodeName = nn;
        MQTT mqtt = new MQTT();
        mqtt.setClientId(nn);
        mqtt.setCleanSession(true);
        mqtt.setHost(url);
        connection = mqtt.callbackConnection();
        connection.listener(this);
        restartPool.schedule(this,0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void notify(final TelemetryEvent event) {

        final String topicName = "nodes/" + nodeName;
        connection.getDispatchQueue().execute(new Runnable() {
            public void run() {
                connection.publish(topicName, event.toJSON().getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                    public void onSuccess(Void v) {
                        Log.debug("Telemetry message published on {}", topicName);
                    }

                    public void onFailure(Throwable value) {
                        Log.error("Error while sending telemetry message", value);
                    }
                });
            }
        });
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        restartPool.schedule(this,1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {

    }

    @Override
    public void onFailure(Throwable value) {
         value.printStackTrace();
    }

    @Override
    public void run() {
        final Runnable self = this;
        Callback callback = new Callback<Void>() {
            public void onFailure(Throwable value) {
                restartPool.schedule(self,5000, TimeUnit.MILLISECONDS);
            }
            public void onSuccess(Void v) {
                Log.info("Connected to Telemetry sever ");
            }
        };
        connection.connect(callback);
    }
}
