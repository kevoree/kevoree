/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.wayoda.ang.libraries.Core;
import org.wayoda.ang.libraries.Library;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;

/**
 * @author ffouquet
 */
public class ArduinoLink {

    private List<String> lCmd;

    public void prepareCommands() {
        String binPrefix = System.getProperty("arduino.home") + "/hardware/tools/avr/bin";
        if (binPrefix != null && !binPrefix.endsWith(File.separator)) {
            binPrefix += File.separator;
        }
        lCmd = new ArrayList<String>();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            lCmd.add(binPrefix + "avr-gcc.exe");
        } else {
            lCmd.add(binPrefix + "avr-gcc");
        }

        lCmd.add("-Os");
        lCmd.add("-Wl,--gc-sections");
    }

    public void linkSketch(Sketch sketch, Target target) {

        /* Now link the core, library and user code */
        List<String> linkCmd = new ArrayList<String>(lCmd);
        linkCmd.add("-mmcu=" + target.getMCU());
        linkCmd.add("-o");
        linkCmd.add(sketch.getBuildRootPath(target) + File.separator + sketch.getName() + ".elf");

        List<String> objectLinked = new ArrayList<String>();


        /* link with every library that was referenced in the sketch */
        for (Library l : sketch.getLibraries()) {
            for (File f : sketch.getLibraryObjectFiles(target, l)) {
                if (!objectLinked.contains(f.getPath())) {
                    objectLinked.add(f.getPath());
                    linkCmd.add(f.getPath());
                }
            }
        }

        /* link all the objects that were compiled for the sketch */
        for (File f : sketch.getObjectFiles(target)) {
            if (!objectLinked.contains(f.getPath())) {
                objectLinked.add(f.getPath());
                linkCmd.add(f.getPath());
            }
        }

        /* link with the core-archive */
        linkCmd.add(sketch.getCoreBuildRoot(target).getPath() + File.separator + Core.CORE_ARCHIVE_NAME);
        linkCmd.add("-L" + sketch.getBuildRootPath(target));
        linkCmd.add("-lm");
        execute(linkCmd);
    }

    protected final int execute(List<String> cmds) {

        String finalCommand = "";
        for (String cmd : cmds) {
            finalCommand = finalCommand + cmd + " ";
        }

        ArduinoCommandExec.execute(finalCommand);

        return 0;
    }
}
