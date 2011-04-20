package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;

import java.io.*;
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
public class OSC2UserNumber {

    @Start
    public void start() {

    }

    @Stop
    public void stop() {

    }

    private AtomicInteger lastComputedValue = new AtomicInteger();

    private Pattern pattern = Pattern.compile(".*<MESSAGE NAME=\"(.*)\">.*");

    @Port(name = "osc")
    public void oscEvent(Object param) {

        Matcher matcher = pattern.matcher(param.toString());
        if (matcher.find()) {
            System.out.println(matcher.group());
        }

    }


}
