
package org.kevoree.library.javase.webserver.jenkins;

import hudson.EnvVars;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import winstone.Launcher;

/**
 *
 * @author duke
 */
@Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "port" , defaultValue = "8080"),
        @DictionaryAttribute(name = "timeout" , defaultValue = "5000", optional = true),
        @DictionaryAttribute(name = "home" , defaultValue = "", optional = true)
})
@ComponentType
public class JenkinsServer extends AbstractComponentType {

    private KevLauncher winstone = null;

    @Start
    public void startServer() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        // this is so that JFreeChart can work nicely even if we are launched as a daemon
        System.setProperty("java.awt.headless", "true");
        // tell Jenkins that Winstone doesn't support chunked encoding.
        if (System.getProperty("hudson.diyChunking") == null) {
            System.setProperty("hudson.diyChunking", "true");
        }
        EnvVars.masterEnvVars.put("JENKINS_HOME", "/Users/duke/Documents/dev/sandbox/jenkinsHome");
        Field f = this.getClass().getClassLoader().loadClass("winstone.WinstoneSession").getField("SESSION_COOKIE_NAME");
        f.setAccessible(true);
        f.set(null, "JSESSIONID." + UUID.randomUUID().toString().replace("-", "").substring(0, 8));

        Map args = new HashMap();
        args.put("webroot", "/Users/duke/Desktop/jenkinsHome");
        //args.put("preferredClassLoader",KevWebappClassLoader.class.getName());
        //args.put("preferredClassLoader",KevWebappClassLoader.class.getName());
        KevLauncher.initLogger(args);


        winstone = new KevLauncher(args);
        /*
        java.io.File tempWar = java.io.File.createTempFile("-t-", "-t-");
         tempWar.delete();
         tempWar.mkdirs();
          org.kevoree.framework.FileNIOHelper.unzipToTempDir(jarFile,tempWar,java.util.Arrays.asList("",".filtered"),java.util.Arrays.asList(".jar"));
        */
    }

    @Stop
    public void stopServer(){
        winstone.shutdown();
    }

    @Update
    public void update() throws IOException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        stopServer();
        startServer();
    }
    
    
}
