package org.kevoree.library.javase.mongoDB;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/08/12
 * Time: 21:32
 */
public class Tester {

    public static void main(String[] args){

        try {
            int port = 12345;
            MongodProcess mongod = null;
            MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_2, port, Network.localhostIsIPv6());
            MongodStarter runtime = MongodStarter.getDefaultInstance();
            MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
            mongod = mongodExecutable.start();
            Mongo mongo = new Mongo("localhost", port);
            DB db = mongo.getDB("test");
            DBCollection col = db.createCollection("testCol", new BasicDBObject());
            col.save(new BasicDBObject("testDoc", new Date()));

            Thread.sleep(5000);

            mongod.stop();

        } catch(Exception e){  }

    }

}
