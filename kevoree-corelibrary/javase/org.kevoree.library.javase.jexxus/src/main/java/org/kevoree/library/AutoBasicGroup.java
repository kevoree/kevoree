package org.kevoree.library;

import jexxus.common.Connection;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Start;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreePropertyHelper;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/12/12
 * Time: 12:10
 */

@GroupType
public class AutoBasicGroup extends BasicGroup {

    private final byte discoveryRequest = 8;
    private final byte discoveryResponse = 9;

    @Override
    protected void externalProcess(byte[] data, Connection from) {
        switch (data[0]) {
            case discoveryRequest: {
                //BROADCAST MY INFO
                broadCastMyInfo();
            }
            break;
            case discoveryResponse: {
                //BROADCAST MY INFO
                processRemoteInfo(data,from.getIP());
            }
            break;
            default:
                from.close();
        }
    }

    protected void broadCastMyInfo() {
        String infos = getName() + ";" + getNodeName() + ";" + getClass().getSimpleName() + ";" + port;
        BroadCastSender.send(port, infos.getBytes());
    }

    private static ModelCloner cloner = new ModelCloner();
    protected void processRemoteInfo(byte[] rawData, final String ip) {
        final String[] data = new String(rawData).split(";");
        if (data.length == 4 && getName().equals(data[0]) && getClass().getSimpleName().equals(data[2])) {
            new Thread() {
                @Override
                public void run() {

                }
            }.start();
        }
    }

    @Start
    public void startRestGroup() throws IOException {
        super.startRestGroup();
        BroadCastSender.send(port, (discoveryRequest + "").getBytes());
    }

}
