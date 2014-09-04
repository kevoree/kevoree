package org.kevoree.bootstrap.telemetry;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.api.telemetry.TelemetryListener;
import org.kevoree.core.impl.TelemetryEventImpl;
import org.kevoree.log.Log;

import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by duke on 8/7/14.
 */
public class MQTTDispatcher implements TelemetryListener, Listener, Runnable {

    String url;

    String nodeName;
    private MQTT mqttClient;
    String topicName;

    private CallbackConnection connection;

    private ScheduledExecutorService restartPool = Executors.newScheduledThreadPool(1);


    public MQTTDispatcher(String u, String nn) throws URISyntaxException {
        this.url = u;
        this.nodeName = nn;
        this.topicName = "nodes/" + nodeName + "/log";
        mqttClient = new MQTT();
        mqttClient.setClientId(nodeName);
        mqttClient.setCleanSession(true);
        mqttClient.setHost(url);
        connection = mqttClient.callbackConnection();
        connection.listener(this);
        restartPool.schedule(this,0, TimeUnit.MILLISECONDS);
    }

    public void closeConnection() {
        notify(TelemetryEventImpl.build(nodeName,"info","Shutting telemetry down.",""));
        //final Semaphore s =new Semaphore(0);
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //s.release();
            }

            @Override
            public void onFailure(Throwable throwable) {
                //s.release();
            }
        });
        /*
        try {
            s.acquire(1000);
        } catch (InterruptedException e) {
            Log.error("Telemetry Disconnection interrupted.", e);
        }
*/
    }

    @Override
    public void notify(final TelemetryEvent event) {
        notify(event, topicName);
    }

    public void notify(final TelemetryEvent event, final String topic) {

        connection.getDispatchQueue().execute(new Runnable() {
            public void run() {
                connection.publish(topic, event.toJSON().getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                    public void onSuccess(Void v) {
                        // Log.debug("Telemetry message published on {}", topicName);
                    }

                    public void onFailure(Throwable value) {
                        Log.error("Error while sending telemetry message: " + event.toJSON(), value);
                    }
                });
            }
        });
    }

    @Override
    public void onConnected() {
        System.out.println("Telemetry connected.");
    }

    @Override
    public void onDisconnected() {
       // restartPool.schedule(this,1000, TimeUnit.MILLISECONDS);
        System.err.println("Telemetry disconnected.");
    }

    @Override
    public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
        ack.run();
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
                //  restartPool.schedule(self,5000, TimeUnit.MILLISECONDS);
                System.err.println("Telemetry connection failed.");
            }
            public void onSuccess(Void v) {
                MQTTDispatcher.this.notify(TelemetryEventImpl.build(nodeName, "info", "Connected to telemetry server", ""));
                System.out.println("Telemetry connection succeeded.");
            }
        };
        connection.connect(callback);

    }
}
