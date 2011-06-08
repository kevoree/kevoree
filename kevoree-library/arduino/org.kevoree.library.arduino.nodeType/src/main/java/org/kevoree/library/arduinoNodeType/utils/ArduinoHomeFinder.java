package org.kevoree.library.arduinoNodeType.utils;

import javax.swing.*;
import java.io.File;

/**
 * User: ffouquet
 * Date: 08/06/11
 * Time: 18:07
 */
public class ArduinoHomeFinder {

    public static boolean checkArduinoHome() {

        if (System.getProperty("arduino.home") == null) {
            System.setProperty("arduino.home", guiAskForArduinoHome());
        }
        String previousArduinoHome = System.getProperty("arduino.home");
        File f = new File(previousArduinoHome);
        if (f.exists()) {
            //CHECK FOR OSX
            String osName = System.getProperty("os.name");
            if (osName.equals("Mac OS X") && f.getName().endsWith(".app")) {
                System.setProperty("arduino.home", f.getAbsolutePath() + "/Contents/Resources/Java");
            }
            return true;
        } else {
            return false;
        }

    }

    private static String guiAskForArduinoHome() {
        JFileChooser arduinoHomeFinder = new JFileChooser();
        int result = arduinoHomeFinder.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return arduinoHomeFinder.getSelectedFile().getAbsolutePath();
        } else {
            return "";
        }
    }


}
