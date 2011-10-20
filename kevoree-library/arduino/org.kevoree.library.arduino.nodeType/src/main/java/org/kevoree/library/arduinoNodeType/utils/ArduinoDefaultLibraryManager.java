/*
package org.kevoree.library.arduinoNodeType.utils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

*/
/**
 * User: ffouquet
 * Date: 08/06/11
 * Time: 20:04
 *//*

public class ArduinoDefaultLibraryManager {

    public static String copyDefaultLibrary() {
        if (ArduinoDefaultLibraryManager.class.getClassLoader().getResource("arduino/library/QueueList/QueueList.h") != null) {
            String arduinoHome = System.getProperty("arduino.home");
            File file = new File(arduinoHome + File.separator + "libraries" + File.separator + "QueueList");
            if (!file.exists()) {
                file.mkdirs();
            }

            // System.out.println(file.getAbsolutePath() + "/QueueList.h");

            //COPY FILE
            if (!new File(file.getAbsolutePath() + "/QueueList.h").exists()) {
                copyFile(ArduinoDefaultLibraryManager.class.getClassLoader().getResourceAsStream("arduino/library/QueueList/QueueList.h")
                        , file.getAbsolutePath() + File.separator + "QueueList.h");
            }
			return arduinoHome + File.separator + "libraries" + File.separator;
        } else {
			return null;
		}
    }


    public static void copyFile(InputStream in, String to) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(to);
            while (true) {
                int data = in.read();
                if (data == -1) {
                    break;
                }
                out.write(data);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ArduinoDefaultLibraryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(ArduinoDefaultLibraryManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ArduinoDefaultLibraryManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
*/
