package org.kevoree.library.javase.nodejs;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;

import java.io.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/08/12
 * Time: 23:34
 */
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8066", optional = true)
})
public class HelloWorldNodeJS extends AbstractNodeJSComponentType {

    @Override
    public String getMainFile() {
        return "hello.js";
    }
             /*
    @Override
    public String getMainDir() {
        File tempDir = createTempDir();
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream(getMainFile()),new File(tempDir,getMainFile()));
        } catch (IOException e) {
            logger.error("Error preparing NodeJS Hello",e);
        }
        return tempDir.getAbsolutePath();
    }

    public static File createTempDir() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        Random rand = new Random();
        int randomInt = 1 + rand.nextInt();
        File tempDir = new File(baseTempPath + File.separator + "tempDir" + randomInt);
        if (tempDir.exists() == false) {
            tempDir.mkdir();
        }
        tempDir.deleteOnExit();
        return tempDir;
    }    */

}
