package org.kevoree.library.javase.webserver.jenkins;

import winstone.Launcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 07:15
 */
public class TesterWinstone {

    public static void main(String[] args) throws Exception {
        Map config = new HashMap();
        config.put("ajp13Port","-1");
        config.put("warfile", "/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.webserver.jenkins/target/jenkins.war");
        Launcher.initLogger(config);
        Launcher winstone = new Launcher(config);
    }

}
