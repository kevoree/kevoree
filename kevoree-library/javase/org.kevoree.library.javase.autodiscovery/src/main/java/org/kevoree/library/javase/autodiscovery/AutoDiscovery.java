package org.kevoree.library.javase.autodiscovery;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;


@ComponentType
public class AutoDiscovery extends AbstractComponentType {

    private List<JmDNS> jmdns =new ArrayList<JmDNS>();

    @Start
    public void start(){

    }

    @Stop
    public void stop(){

    }


}
