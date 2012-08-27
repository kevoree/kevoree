package org.kevoree.library.javase.nodejs;

import de.flapdoodle.embed.nodejs.NodejsPaths;
import de.flapdoodle.embed.process.distribution.Distribution;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 27/08/12
 * Time: 18:51
 */
public class ExtNodejsPaths extends NodejsPaths {

    @Override
    public String getPath(Distribution distribution) {
        if (System.getProperty("os.arch").equals("arm")) {
            return "node-v0.8.8-linux-arm.tar.gz";
        } else {
            return super.getPath(distribution);
        }

    }

}
