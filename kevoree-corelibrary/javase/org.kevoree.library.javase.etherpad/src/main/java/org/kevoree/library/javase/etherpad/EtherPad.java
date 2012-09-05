package org.kevoree.library.javase.etherpad;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.framework.FileNIOHelper;
import org.kevoree.library.javase.nodejs.AbstractNodeJSComponentType;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            return "node"+File.separator+"server.js";
        } else {
            return "node_modules"+File.separator+"ep_etherpad-lite"+File.separator+"node"+File.separator+"server.js";
        }
    }

    @Override
    public String getMainDir() {
        File tempDir = createTempDir();
        try {
            org.apache.commons.io.FileUtils.cleanDirectory(tempDir);
        } catch (IOException e) {
            logger.error("Error while cleaning dir");
        }
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            logger.info("Windows detected, downloading EtherPad-Win, Please wait");
            try {
                File outF = new File(tempDir,"etherpad-lite.zip");
                org.apache.commons.io.FileUtils.forceMkdir(tempDir);
                org.apache.commons.io.FileUtils.copyURLToFile(new URL("http://etherpad.org/etherpad-lite-win.zip"),outF);
                logger.info("Download in {}",outF.getAbsolutePath());
                unzip(outF,tempDir);
                outF.delete();
                org.apache.commons.io.FileUtils.moveDirectory(new File(tempDir.getAbsolutePath()+File.separator+"etherpad-lite-win"),new File(tempDir.getAbsolutePath()+File.separator+"etherpad-lite"));
            } catch (Exception e) {
                logger.error("Error while preparing etherpad lite windows ",e);
            }
        } else {
            FileNIOHelper.unzipToTempDir(getClass().getClassLoader().getResourceAsStream("etherpad-lite.zip"), tempDir, new ArrayList<String>(), new ArrayList<String>());
        }
        logger.info("Extract EtherPad to dir : "+tempDir.getAbsolutePath());
        try {
            copyparam(tempDir);
        } catch (IOException e) {
            logger.error("Error while update param",e);
        }
        return tempDir.getAbsolutePath()+File.separator+"etherpad-lite";
    }

    public static List<File> unzip(File zipFile, File targetDir) throws IOException {
        List<File> files = new ArrayList<File>();
        ZipFile zip = new ZipFile(zipFile);
        try {
            zip = new ZipFile(zipFile);
            for (ZipEntry entry : Collections.list(zip.entries())) {
                if(entry.isDirectory()){

                } else {
                    InputStream input = zip.getInputStream(entry);
                    try {
                        if (!targetDir.exists()) targetDir.mkdirs();
                        String cleanName = entry.getName();
                        if(entry.getName().contains("?")){
                            cleanName = cleanName.substring(0,cleanName.indexOf("?"));
                            System.out.println("Clean "+cleanName);
                        }
                        File target = new File(targetDir,cleanName);
                        FileUtils.copyInputStreamToFile(input, target);
                        files.add(target);
                    } finally {
                        IOUtils.closeQuietly(input);
                    }
                }

            }
            return files;
        } finally {
            zip.close();
        }
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

