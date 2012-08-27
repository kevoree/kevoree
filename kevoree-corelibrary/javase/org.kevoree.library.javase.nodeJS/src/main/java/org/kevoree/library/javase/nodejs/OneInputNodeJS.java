package org.kevoree.library.javase.nodejs;

import org.kevoree.annotation.ComponentType;
import org.kevoree.framework.FileNIOHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 24/08/12
 * Time: 19:46
 */
@ComponentType
public class OneInputNodeJS extends AbstractNodeJSComponentType {


    @Override
    public String getMainFile() {
        return "oneInput.js";
    }

    /*
    @Override
    public String getMainDir() {
        try {
            File tempDir = createTempDir();
            org.apache.commons.io.FileUtils.forceMkdir(tempDir);
            FileNIOHelper.unzipToTempDir(getClass().getClassLoader().getResourceAsStream("node_modules.zip"), tempDir, new ArrayList<String>(), new ArrayList<String>());
            org.apache.commons.io.FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream(getMainFile()),new File(tempDir,getMainFile()));
        } catch (IOException e) {
            logger.error("Error while preparing directory");
        }
        return "";
    }

    public File createTempDir() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        Random rand = new Random();
        int randomInt = 1 + rand.nextInt();
        File tempDir = new File(baseTempPath + File.separator + "tempDir" + randomInt);
        if (tempDir.exists() == false) {
            tempDir.mkdir();
        }
        tempDir.deleteOnExit();
        return tempDir;
    }  */

}
