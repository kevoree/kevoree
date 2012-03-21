package org.kevoree.library.javase.webserver.jenkins;


import java.util.*;

import org.kevoree.framework.FileHelper;
import winstone.Launcher;


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 07:15
 */
public class TesterWinstone {

    public static void main(String[] args) throws Exception {


/*
        System.out.println(TesterWinstone.class.getClassLoader().getResource("javax/servlet/resources/web-app_2_5.xsd"));

        JenkinsServer server = new JenkinsServer();
        server.startServer();
  */
        
        
        //Main.main(args);




        System.out.println(TesterWinstone.class.getClassLoader().getParent());
        System.setProperty("JENKINS_HOME","/Users/duke/Documents/dev/sandbox/jenkinsHome");
        Map config = new HashMap();
        config.put("ajp13Port","-1");
        config.put("webroot", "/Users/duke/Downloads/jenkins");
        Launcher.initLogger(config);
        Launcher winstone = new Launcher(config);
    }

}
