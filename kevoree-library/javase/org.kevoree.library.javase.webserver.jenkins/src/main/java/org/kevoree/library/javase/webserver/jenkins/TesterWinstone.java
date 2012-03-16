package org.kevoree.library.javase.webserver.jenkins;

import hudson.EnvVars;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.codehaus.groovy.tools.shell.Main;


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 07:15
 */
public class TesterWinstone {

    public static void main(String[] args) throws Exception {



        System.out.println(TesterWinstone.class.getClassLoader().getResource("javax/servlet/resources/web-app_2_5.xsd"));

        JenkinsServer server = new JenkinsServer();
        server.startServer();
        
        
        
        //Main.main(args);
        
        
        
        
        /*
        System.out.println(TesterWinstone.class.getClassLoader().getParent());
        
        Map config = new HashMap();
        config.put("ajp13Port","-1");
        config.put("warfile", "/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.webserver.jenkins/target/jenkins.war");
        Launcher.initLogger(config);
        Launcher winstone = new Launcher(config);*/
    }

}
