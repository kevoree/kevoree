package org.kevoree.library.javase.nodejs;

import de.flapdoodle.embed.nodejs.NodejsDownloadConfig;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 27/08/12
 * Time: 18:43
 */
public class ExtNodejsDownloadConfig extends NodejsDownloadConfig {

    @Override
    public String getDownloadPath() {
        if (System.getProperty("os.arch").equals("arm")) {
            setDownloadPath("http://cloud.github.com/downloads/dukeboard/kevoree/");
            return super.getDownloadPath();
        } else {
            return super.getDownloadPath();
        }
    }

}
