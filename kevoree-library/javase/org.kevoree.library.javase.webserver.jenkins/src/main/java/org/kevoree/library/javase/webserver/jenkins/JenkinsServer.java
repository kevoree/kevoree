package org.kevoree.library.javase.webserver.jenkins;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author duke
 */
@Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8080"),
        @DictionaryAttribute(name = "home", defaultValue = "", optional = true)
})

@ComponentType
public class JenkinsServer extends AbstractComponentType {

    private KevLauncher winstone = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void startServer() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        // this is so that JFreeChart can work nicely even if we are launched as a daemon
        System.setProperty("java.awt.headless", "true");
        // tell Jenkins that Winstone doesn't support chunked encoding.
        if (System.getProperty("hudson.diyChunking") == null) {
            System.setProperty("hudson.diyChunking", "true");
        }

        if(this.getDictionary().get("home") != null && !this.getDictionary().get("home").equals("")){
            System.setProperty("JENKINS_HOME", this.getDictionary().get("home").toString());
        } else {
            File tempUserDir = new File(System.getProperty("java.io.tmpdir")+File.separator+"jenkinsHome"+getName());
            tempUserDir.mkdirs();
            System.setProperty("JENKINS_HOME", tempUserDir.getAbsolutePath());
        }
        logger.info("Jenkins User Home at "+System.getProperty("JENKINS_HOME"));
        //   EnvVars.masterEnvVars.put("JENKINS_HOME", "/Users/duke/Documents/dev/sandbox/jenkinsHome");
        Field f = this.getClass().getClassLoader().loadClass("winstone.WinstoneSession").getField("SESSION_COOKIE_NAME");
        f.setAccessible(true);
        f.set(null, "JSESSIONID." + UUID.randomUUID().toString().replace("-", "").substring(0, 8));

        Map args = new HashMap();
        //args.put("preferredClassLoader",KevWebappClassLoader.class.getName());
        //args.put("preferredClassLoader",KevWebappClassLoader.class.getName());
        KevLauncher.initLogger(args);
        args.put("ajp13Port", "-1");

        java.io.File tempWarDir = java.io.File.createTempFile("-t-", "-t-");
        tempWarDir.delete();
        tempWarDir.mkdirs();

        org.kevoree.framework.FileNIOHelper.unzipToTempDir(this.getClass().getClassLoader().getResourceAsStream("jenkins.war"), tempWarDir, java.util.Arrays.asList(".filtered"), java.util.Arrays.asList(".filtered"));
        args.put("webroot", tempWarDir.getAbsolutePath());
        args.put("httpPort",this.getDictionary().get("port"));
        winstone = new KevLauncher(args);
    }


    @Stop
    public void stopServer() {
        winstone.shutdown();
    }

    @Update
    public void update() throws IOException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        stopServer();
        startServer();
    }


}
