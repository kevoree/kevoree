package org.kevoree.library.javase.webserver.wordpress;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 15/12/11
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class ZipHelper {

    public static void main(String[] ags){
        System.out.println(unzipToTempDir(ZipHelper.class.getClassLoader().getResourceAsStream("wordpress-3.3-fr_FR.zip")).getAbsolutePath());
    }
    

    public static File unzipToTempDir(InputStream res) {
        try {
            File tempDir = File.createTempFile("tempDir", "kevTemp");
            tempDir.delete();
            tempDir.mkdir();
            ZipInputStream zis = new ZipInputStream(res);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(tempDir.getAbsolutePath() + File.separator + entry.getName()).mkdirs();
                } else {
                    BufferedOutputStream outputEntry = new BufferedOutputStream(new FileOutputStream(new File(tempDir + File.separator + entry.getName())));
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while (zis.available() > 0) {
                        len = zis.read(buffer);
                        if(len > 0){
                            outputEntry.write(buffer, 0, len);
                        }
                    }
                    outputEntry.flush();
                    outputEntry.close();
                }
            }
            zis.close();
            return tempDir;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
