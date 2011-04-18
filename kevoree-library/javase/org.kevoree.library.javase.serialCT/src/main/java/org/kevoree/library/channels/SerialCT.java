package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.channels.utils.SerialReader;
import org.kevoree.library.channels.utils.SerialWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnu.io.SerialPort;

import java.util.logging.Level;

/**
 * @author ffouquet
 */
@Library(name = "KevoreeArduinoJava")
@ChannelTypeFragment
public class SerialCT extends AbstractChannelFragment {

    private Logger logger = LoggerFactory.getLogger(SerialCT.class);
    protected SerialReader serialReader;
    protected SerialWriter serialWriter;
    protected SerialPort serialPort;


    @Start
    public void start() {
        logger.info("Starting " + this.getClass().getName() + " " + this.getName());
        try {
            serialPort = RxTxHelper.connect((String) this.getDictionary().get("port"), this);
            readSerialPort();
            writeSerialPort();
        } catch (Exception ex) {
            logger.error("Error : ",ex);
        }
    }

    public void update() {
        stop();
        start();
    }

    @Stop
    public void stop() {
        logger.info("Stopping " + this.getClass().getName() + " " + this.getName());
        if (serialReader != null) {
            serialReader.shutdown();
        }
        if (serialWriter != null) {
            serialWriter.shutdown();
        }
        if (serialPort != null) {
            serialPort.close();
        }
    }


    @Override
    public Object dispatch(Message msg) {

        System.out.println("Local node bsize" + getBindedPorts().size());

        if (getBindedPorts().isEmpty() && getOtherFragments().isEmpty()) {
            System.out.println("No consumer, msg lost=" + msg.getContent());
        }
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!msg.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender(){

            @Override
            public Object sendMessageToRemote(Message message) {
                return null;
            }
        };
    }
}
