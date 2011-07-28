/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;

/**
 * @author ffouquet
 */
public class ArduinoPostCompilation {

    private List<String> eepromCmd;
    private List<String> hexCmd;

    public void prepareCommands() {
        eepromCmd = new ArrayList<String>();
        eepromCmd.add(ArduinoToolChainExecutables.getAVR_OBJCOPY());
        eepromCmd.add("-O");
        eepromCmd.add("ihex");
        eepromCmd.add("-j");
        eepromCmd.add(".eeprom");
        eepromCmd.add("--set-section-flags=.eeprom=alloc,load");
        eepromCmd.add("--no-change-warnings");
        eepromCmd.add("--change-section-lma");
        eepromCmd.add(".eeprom=0");

        hexCmd = new ArrayList<String>();
        hexCmd.add(ArduinoToolChainExecutables.getAVR_OBJCOPY());
        hexCmd.add("-O");
        hexCmd.add("ihex");
        hexCmd.add("-R");
        hexCmd.add(".eeprom");
    }

    public void postCompileSketch(Sketch sketch, Target target) {

        /* 
        Build the two files (flash- and eeprom-code) 
        that get uploaded to the board 
         */
        List<String> eepromBuildCmd = new ArrayList<String>(eepromCmd);
        eepromBuildCmd.add(sketch.getBuildRootPath(target) + File.separator + sketch.getName() + ".elf");
        eepromBuildCmd.add(sketch.getBuildRootPath(target) + File.separator + sketch.getName() + ".eep");
        execute(eepromBuildCmd);

        List<String> hexBuildCmd = new ArrayList<String>(hexCmd);
        hexBuildCmd.add(sketch.getBuildRootPath(target) + File.separator + sketch.getName() + ".elf");
        hexBuildCmd.add(sketch.getBuildRootPath(target) + File.separator + sketch.getName() + ".hex");
        execute(hexBuildCmd);
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
