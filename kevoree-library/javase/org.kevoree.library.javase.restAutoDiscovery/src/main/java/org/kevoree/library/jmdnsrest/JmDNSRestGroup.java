package org.kevoree.library.jmdnsrest;

import org.kevoree.ContainerRoot;
import org.kevoree.DictionaryValue;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.kevoree.library.rest.RestGroup;
import org.kevoree.library.rest.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 18:27
 */


@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true , fragmentDependant = true)
})
@GroupType
@Library(name="JavaSE")
public class JmDNSRestGroup extends RestGroup {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JmDnsComponent jmdns = null ;

    @Start
    public void startJmdnsGroup() {
        super.startRestGroup();
        jmdns = new JmDnsComponent(this.getNodeName(),this.getName() ,Integer.parseInt(this.getDictionary().get("port").toString()),this.getModelService(),"JmDNSRestGroup");
    }

    @Stop
    public void stopJmdnsGroup() {
        jmdns.close();
        jmdns = null;
        super.stopRestGroup();
    }


}
