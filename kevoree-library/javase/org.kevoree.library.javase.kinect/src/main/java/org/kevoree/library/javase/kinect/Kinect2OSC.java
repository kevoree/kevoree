package org.kevoree.library.javase.kinect;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.library.javase.kinect.osc.OscServer;


@DictionaryType({
        @DictionaryAttribute(name = "PORT", optional = false, defaultValue = "57111")
})
@Requires({
        @RequiredPort(name = "osc", type = PortType.MESSAGE)
})
@Library(name = "JavaSE")
@ComponentType
public class Kinect2OSC extends AbstractComponentType {

    private OscServer oscServer;

    private int parsePortNumber() {
        String portProperty = this.getDictionary().get("PORT").toString();
        return Integer.parseInt(portProperty);
    }

    @Start
    public void start() {
        oscServer = new OscServer(parsePortNumber(), this);
        oscServer.start();
    }

    @Stop
    public void stop() {
        oscServer.killServer();
        oscServer = null;
    }

    public void send(String message) {
        if (isPortBinded("osc")) {
            getPortByName("osc", MessagePort.class).process(message);
        }
    }



}
