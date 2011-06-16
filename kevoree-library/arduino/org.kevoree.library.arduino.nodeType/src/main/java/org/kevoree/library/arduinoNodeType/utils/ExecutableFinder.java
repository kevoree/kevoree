package org.kevoree.library.arduinoNodeType.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * User: ffouquet
 * Date: 09/06/11
 * Time: 16:37
 */
public class ExecutableFinder {

    private static List<String> pathDirectories = new ArrayList<String>();

    //ONLY FOR UNIX / windows must use cmd.exe
    /* Try to build system path */
    private static List<String> getSysPaths() {
        if (pathDirectories.isEmpty()) {
            if (System.getenv() != null) {
                if (System.getenv().get("PATH") != null) {
                    String line = System.getenv().get("PATH").toString();
                    String[] paths = line.split(File.pathSeparator);
                    for (int i = 0; i < paths.length; i++) {
                        pathDirectories.add(paths[i]);
                    }
                }
            }
        }
        return pathDirectories;
    }

    public static String getAbsolutePath(String execName, List<String> otherPaths) {
        String path = "";
        for (String s : getSysPaths()) {
            try {
                Process p = Runtime.getRuntime().exec(s + "/" + execName);
                path = s + "/" + execName;
            } catch (IOException e) {
                //NOTHING
            }
        }
        if (otherPaths != null) {
            for (String s : otherPaths) {
                try {
                    Process p = Runtime.getRuntime().exec(s + "/" + execName);
                    path = s + "/" + execName;
                } catch (IOException e) {
                    //NOTHING
                }
            }
        }
        return path;
    }
}
