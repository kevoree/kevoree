package org.kevoree.library.javase.nodejs;

import com.google.common.collect.Lists;
import de.flapdoodle.embed.nodejs.*;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/08/12
 * Time: 22:59
 */
@Library(name = "JavaSE")
@ComponentFragment
public abstract class AbstractNodeJSComponentType extends AbstractComponentType {

    public abstract String getMainFile();

    public abstract String getMainDir();

    private Thread t = null;
    NodejsProcess node = null;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() {
        t = new Thread() {
            @Override
            public void run() {
                NodejsProcess node = null;
                NodejsRuntimeConfig runtimeConfig = new NodejsRuntimeConfig();
                ExtNodejsDownloadConfig dwlConfig = new ExtNodejsDownloadConfig();
                dwlConfig.setPackageResolver(new ExtNodejsPaths());
                runtimeConfig.setDownloadConfig(dwlConfig);


                List<String> params = new ArrayList<String>();
                for(String key : getDictionary().keySet()){
                    params.add(key+"="+getDictionary().get(key));
                }
                NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.V0_8_6, getMainFile(), params, getMainDir());
                NodejsStarter runtime = new NodejsStarter(runtimeConfig);
                NodejsExecutable nodeExecutable = runtime.prepare(nodejsConfig);
                try {
                    node = nodeExecutable.start();
                } catch (IOException e) {
                    logger.error("Error while starting nodeJS", e);
                }
            }
        };
        t.start();
    }

    @Stop
    public void stop() {
        try {
           t.interrupt();
        } catch (Exception e) {

        } finally {
            t = null;
        }
        try {
            node.stop();
        } catch (Exception e) {

        } finally {
            node = null;
        }
    }

    @Update
    public void update() {
         stop();start();
    }
}
