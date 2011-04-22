package org.kevoree.library.channels;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * @author ffouquet
 */
@Library(name = "KevoreeArduinoJava")
@DictionaryType({
        @DictionaryAttribute(name = "PORT", optional = false, defaultValue = "/dev/ttyS0")
})
@ChannelTypeFragment
public class SerialCT extends AbstractChannelFragment {

    private Logger logger = LoggerFactory.getLogger(SerialCT.class);
    protected SerialReader serialReader;
    protected SerialWriter serialWriter;
    protected SerialPort serialPort;


    @Start
    public void startRxTxChannel() {
        logger.info("Starting " + this.getClass().getName() + " " + this.getName());
        try {

            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier((String) this.getDictionary().get("PORT"));
            CommPort commPort = portIdentifier.open(this.getNodeName() + ":" + this.getName(), 2000);
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
            //    serialPort.addEventListener(new SerialReader(serialPort.getInputStream()));
            //    serialPort.notifyOnDataAvailable(true);
                writeSerialPort();
                readSerialPort();

            } else {
                System.out.println("Error: Only serial ports are handled by this component.");
                commPort.close();
            }

        } catch (Exception ex) {
            logger.error("Error : ", ex);
        }
    }

    @Update
    public void updateRxTxChannel() {
        stopRxTxChannel();
        startRxTxChannel();
    }

    @Stop
    public void stopRxTxChannel() {
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

        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (msg.getPassedNodes().isEmpty()) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                if (serialWriter != null) {
                    logger.info("Send message = " + message.getContent());
                    serialWriter.sendMsg(message.getContent().toString()+";");
                }
                return null;
            }
        };
    }

    public void restartSerialReader() {
        readSerialPort();
        logger.warn("A problem with the serial port has been detected. Trying to reconnect on the same port. It this does not work, please update the attributes of the component with proper port.");
    }

    protected void readSerialPort() {
        InputStream in = null;
        try {
            in = serialPort.getInputStream();
            serialReader = new SerialReader(in, this);
            serialReader.start();
        } catch (IOException ex) {
            logger.error("Error : ", ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error("Error : ", ex);
            }
        }
    }

    protected void writeSerialPort() {
        try {
            OutputStream out = serialPort.getOutputStream();
            serialWriter = new SerialWriter(out);
            serialWriter.start();
        } catch (IOException ex) {
            logger.error("Error : ", ex);
        }
    }


}
