package org.kevoree.library.javase.webserver.gitblit;

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
public class GitBlitServer extends AbstractComponentType {

    private KevLauncher winstone = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void startServer() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        Map args = new HashMap();
        //args.put("preferredClassLoader",KevWebappClassLoader.class.getName());
        //args.put("preferredClassLoader",KevWebappClassLoader.class.getName());
        KevLauncher.initLogger(args);
        args.put("ajp13Port", "-1");

        java.io.File tempWarDir = java.io.File.createTempFile("-t-", "-t-");
        tempWarDir.delete();
        tempWarDir.mkdirs();

        org.kevoree.framework.FileNIOHelper.unzipToTempDir(this.getClass().getClassLoader().getResourceAsStream("gitblit-0.8.2.war"), tempWarDir, java.util.Arrays.asList(".filtered"), java.util.Arrays.asList(".filtered"));
        args.put("webroot", tempWarDir.getAbsolutePath());
        args.put("httpPort",this.getDictionary().get("port"));
        winstone = new KevLauncher(args);
    }


    @Stop
    public void stopServer() {
        if(winstone != null){
            winstone.shutdown();
        }
    }

    @Update
    public void update() throws IOException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        stopServer();
        startServer();
    }


}
