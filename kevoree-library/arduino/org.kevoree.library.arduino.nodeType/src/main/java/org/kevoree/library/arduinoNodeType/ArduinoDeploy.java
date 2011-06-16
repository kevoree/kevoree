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
public class ArduinoDeploy {

    protected ArrayList<String> baseCmd;
    /**
     * @parameter
     */
    private String baudrate;

    public void prepareCommands() {

        /*
        String binPrefix = System.getProperty("arduino.home") + "/hardware/tools/avr/bin";
        if (binPrefix != null && !binPrefix.endsWith(File.separator)) {
            binPrefix += File.separator;
        } */

        baseCmd = new ArrayList<String>();
        baseCmd.add(ArduinoToolChainExecutables.getAVRDUDE());
        String confPath = System.getProperty("arduino.home") + "/hardware/tools/avr/etc/avrdude.conf";
        if (confPath != null) {
            baseCmd.add("-C");
            baseCmd.add(confPath);
        }
    }

    public final void uploadSketch(Sketch sketch, Target target, String portName) {

        File hexFile = sketch.getFlash(target);
        if (hexFile == null) {
            System.err.println("No upload data found");
            throw new IllegalStateException();
        }

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.addAll(baseCmd);
        cmd.add("-q");
        cmd.add("-q");
        cmd.add("-c");
        cmd.add("stk500v1");
        cmd.add("-p");
        cmd.add(target.getMCU());
        cmd.add("-P");
        cmd.add(portName);
        cmd.add("-b");
        cmd.add(baudrate != null ? baudrate : target.getUploadSpeed());
        cmd.add("-D");
        cmd.add("-Uflash:w:" + hexFile.getPath() + ":i");
        execute(cmd);
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
