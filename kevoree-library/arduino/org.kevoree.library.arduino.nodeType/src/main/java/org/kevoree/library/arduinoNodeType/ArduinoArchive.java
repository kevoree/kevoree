/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.wayoda.ang.libraries.Core;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;
import org.wayoda.ang.utils.FileSelector;
import org.wayoda.ang.utils.FileUtils;

/**
 * @author ffouquet
 */
public class ArduinoArchive {

    private List<String> archCmd;

    public void prepareCommands() {
        archCmd = new ArrayList<String>();
        archCmd.add(ArduinoToolChainExecutables.getAVR_AR());
        archCmd.add("rcs");
    }

    public void archiveSketch(Sketch sketch, Target target) {

        //check that the build directory for the core exists
        File outputDir = sketch.getCoreBuildRoot(target);
        if (outputDir == null) {
            System.err.println("Compiling Core failed. Output directory `" + sketch.getBuildRootPath(target) + File.separator + "core` does not exist or cannot be read or written");
            throw new IllegalStateException();
        }

        for (File f : FileUtils.getFiles(outputDir, new FileSelector.ObjectFileFilter())) {
            ArrayList<String> arcmd = new ArrayList<String>(archCmd);
            arcmd.add(outputDir.getPath() + File.separator + Core.CORE_ARCHIVE_NAME);
            arcmd.add(f.getPath());
            execute(arcmd);
        }
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
