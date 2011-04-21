package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;
import java.util.concurrent.atomic.AtomicInteger;

@DictionaryType({
        @DictionaryAttribute(name = "smoothTime", defaultValue = "1000")
})
@Provides({
        @ProvidedPort(name = "osc", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "number", type = PortType.MESSAGE)
})
@Library(name = "JavaSE")
@ComponentType
public class OSC2UserNumber extends AbstractComponentType implements Runnable {

    Thread sender = null;

    @Start
    public void start() {
        sendValue = true;
        sender = new Thread(this);
        sender.start();
    }

    @Stop
    public void stop() {
        sendValue = false;
    }

    private Set<Integer> ids = new HashSet<Integer>();

    private Pattern pattern = Pattern.compile(".*<MESSAGE NAME=\"(.*)\">.*");

    @Port(name = "osc")
    public void oscEvent(Object param) {

        Matcher m = pattern.matcher(param.toString());
        boolean b = m.matches();
        if (b) {
            for (int i = 0; i <= m.groupCount(); i++) {
                String groupName = m.group(i);
                if (groupName != null && !groupName.equals("")) {
                    String[] groupValue = groupName.split("/");
                    if (groupValue.length >= 3) {
                        if (groupValue[1].equals("user")) {
                            String userNumber = groupValue[2];
                            try {
                                Integer value = Integer.parseInt(userNumber);
                                ids.add(value);
                            } catch (Exception e) {
                                //IGNORE
                            }
                        }
                    }
                }
            }
        }

    }

    private Boolean sendValue = true;

    @Override
    public void run() {
        Integer time = Integer.parseInt(this.getDictionary().get("smoothTime").toString());
        while (sendValue) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.getPortByName("number", MessagePort.class).process(new Integer(ids.size()));
            ids.clear();
        }

    }
}
