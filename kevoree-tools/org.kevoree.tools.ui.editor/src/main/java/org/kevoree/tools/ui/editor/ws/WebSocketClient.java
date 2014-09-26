package org.kevoree.tools.ui.editor.ws;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.json.JSONModelLoader;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/12/2013
 * Time: 12:27
 */
public class WebSocketClient {

    private static JSONModelLoader loader = new JSONModelLoader(new DefaultKevoreeFactory());
    private static JSONModelSerializer saver = new JSONModelSerializer();


    public static void push(String ip, String port, final ContainerRoot model) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().build();
        final AsyncHttpClient c = new AsyncHttpClient(cf);
        WebSocket websocket = c.prepareGet("ws://" + ip + ":" + port)
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                        new WebSocketTextListener() {
                            @Override
                            public void onMessage(String message) {

                            }

                            @Override
                            public void onFragment(String s, boolean b) {
                            }

                            @Override
                            public void onOpen(WebSocket websocket) {
                                websocket.sendTextMessage(saver.serialize(model));
                            }

                            @Override
                            public void onClose(WebSocket websocket) {
                            }

                            @Override
                            public void onError(Throwable t) {
                            }
                        }

                ).build()
                ).get();
    }


    public static void pull(String ip, String port, final ModelCallBack callback) throws IOException, ExecutionException, InterruptedException {

        Log.info("Pull from ws://" + ip + ":" + port);

        AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().build();
        final AsyncHttpClient c = new AsyncHttpClient(cf);
        WebSocket websocket = c.prepareGet("ws://" + ip + ":" + port)
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                        new WebSocketTextListener() {
                            @Override
                            public void onMessage(String message) {
                                try {
                                    callback.run((ContainerRoot) loader.loadModelFromString(message).get(0));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    c.closeAsynchronously();
                                }

                            }

                            @Override
                            public void onFragment(String s, boolean b) {
                            }

                            @Override
                            public void onOpen(WebSocket websocket) {
                                websocket.sendTextMessage("get");
                            }

                            @Override
                            public void onClose(WebSocket websocket) {
                            }

                            @Override
                            public void onError(Throwable t) {
                            }
                        }

                ).build()
                ).get();
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {


    }

}
