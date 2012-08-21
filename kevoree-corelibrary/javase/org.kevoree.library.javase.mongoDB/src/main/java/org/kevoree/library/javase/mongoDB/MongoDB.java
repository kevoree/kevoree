package org.kevoree.library.javase.mongoDB;

import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/08/12
 * Time: 21:42
 */
@Library(name = "JavaSE")
/*@Provides({
        @ProvidedPort(name = "dbm", type = PortType.SERVICE)
})  */
@DictionaryType({
        @DictionaryAttribute(name = "version", defaultValue = "2.0", optional = true, vals = {"2.0", "2.1", "2.2"}),
        @DictionaryAttribute(name = "port", defaultValue = "20000", optional = true)
})
@ComponentType
public class MongoDB extends AbstractComponentType {

    MongodProcess mongod = null;
    Mongo mongo = null;
    Thread runnerThread = null;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() throws IOException {
        runnerThread = new Thread() {
            @Override
            public void run() {
                final int port = Integer.parseInt(getDictionary().get("port").toString());
                Version v = Version.V2_0_6;
                if (getDictionary().get("version").toString().equals("2.0")) {
                    v = Version.V2_0_6;
                }
                if (getDictionary().get("version").toString().equals("2.1")) {
                    v = Version.V2_1_2;
                }
                if (getDictionary().get("version").toString().equals("2.2")) {
                    v = Version.V2_2_0_RC0;
                }
                try {
                    MongodConfig mongodConfig = new MongodConfig(v, port, Network.localhostIsIPv6());
                    MongodStarter runtime = MongodStarter.getDefaultInstance();
                    final MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
                    mongod = mongodExecutable.start();
                    mongo = new Mongo("localhost", port);
                } catch (Exception e) {
                    logger.error("Error while starting MongoDB ", e);
                }
            }
        };
        runnerThread.start();
    }

    @Stop
    public void stop() {
        try {
            runnerThread.interrupt();
        } catch (Exception e) {
        }
        if (mongo != null) {
            mongo.close();
        }
        if (mongod != null) {
            mongod.stop();
        }
    }

    @Update
    public void update() throws IOException {
        stop();
        start();
    }
}
