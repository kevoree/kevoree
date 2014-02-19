package org.kevoree.platform.standalone.test;

import org.jeromq.*;
import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.serializer.JSONModelSerializer;

import java.util.concurrent.CountDownLatch;

/**
 * Created by duke on 19/02/2014.
 */
public class JeroMQCoreWrapper implements Runnable {

    private KevoreeCoreBean core;
    private ZMQ.Socket worker;
    private JSONModelSerializer saver = new JSONModelSerializer();
    private JSONModelLoader loader = new JSONModelLoader();


    public JeroMQCoreWrapper(KevoreeCoreBean coreBean, Integer port) {
        core = coreBean;
        ZMQ.Context context = ZMQ.context(1);
        worker = context.socket(ZMQ.REP);
        worker.bind("tcp://*:" + port);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String call = worker.recvStr();
            if (call.equals("getModel")) {
                worker.send(saver.serialize(core.getCurrentModel().getModel()));
            } else {
                if (call.equals("pushScript")) {
                    worker.send("ok");
                    String script = worker.recvStr();
                    final CountDownLatch latch = new CountDownLatch(1);
                    final Boolean[] result = {false};
                    core.submitScript(script, new UpdateCallback() {
                        @Override
                        public void run(Boolean aBoolean) {
                            result[0] = aBoolean;
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    worker.send(result[0].toString());
                } else {
                    if (call.equals("pushModel")) {
                        worker.send("ok");
                        String modelTxt = worker.recvStr();
                        final CountDownLatch latch = new CountDownLatch(1);
                        final Boolean[] result = {false};
                        ContainerRoot model = (ContainerRoot) loader.loadModelFromString(modelTxt).get(0);
                        core.update(model, new UpdateCallback() {
                            @Override
                            public void run(Boolean aBoolean) {
                                result[0] = aBoolean;
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        worker.send(result[0].toString());
                    } else {
                        if (call.equals("ping")) {
                            worker.send("pong");
                        }
                    }
                }
            }
        }
    }

}
