package org.kevoree.library.javase.etherpad;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.framework.FileNIOHelper;
import org.kevoree.library.javase.nodejs.AbstractNodeJSComponentType;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/08/12
 * Time: 00:41
 */
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "19000", optional = true)
})
public class EtherPad extends AbstractNodeJSComponentType {
    @Override
    public String getMainFile() {
        return "node_modules/ep_etherpad-lite/node/server.js";
       // return "node/server.js";
    }

    @Override
    public String getMainDir() {
       ///return "/Users/duke/Documents/dev/sandbox/etherpad-lite";


        File tempDir = createTempDir();
        FileNIOHelper.unzipToTempDir(getClass().getClassLoader().getResourceAsStream("etherpad-lite.zip"),tempDir,new ArrayList<String>(),new ArrayList<String>());
         logger.info("Extract EtherPad to dir : "+tempDir.getAbsolutePath());
        try {
            copyparam(tempDir);
        } catch (IOException e) {
            logger.error("Error while update param",e);
        }
        return tempDir.getAbsolutePath()+File.separator+"etherpad-lite";
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
    }

    public void copyparam(File target) throws IOException {
        Reader reader = new InputStreamReader(EtherPad.class.getClassLoader().getResourceAsStream("settings.json"));
        BufferedReader bread = new BufferedReader(reader);
        FileWriter fw = new FileWriter(target+File.separator+"etherpad-lite"+File.separator+"settings.json");
        BufferedWriter bw = new BufferedWriter(fw);
        String line = null;
        while((line=bread.readLine()) != null) {
            line = line.replace("etherpad.port.toReplace",getDictionary().get("port").toString());
            bw.write(line);
            bw.write("\n");
        }
        bw.close();
        bread.close();
    }


}

