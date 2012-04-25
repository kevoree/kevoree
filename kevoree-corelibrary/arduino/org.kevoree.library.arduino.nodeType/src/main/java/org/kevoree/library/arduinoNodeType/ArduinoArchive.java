/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wayoda.ang.libraries.Core;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;
import org.wayoda.ang.utils.FileSelector;
import org.wayoda.ang.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ffouquet
 */
public class ArduinoArchive {
	private static final Logger logger = LoggerFactory.getLogger(ArduinoArchive.class);

    private List<String> archCmd;

    public void prepareCommands() {
        archCmd = new ArrayList<String>();
        archCmd.add(ArduinoToolChainExecutables.getAVR_AR());
        archCmd.add("rcsv");
    }

    public void archiveSketch(Sketch sketch, Target target) {

        //check that the build directory for the core exists
        File outputDir = sketch.getCoreBuildRoot(target);
        if (outputDir == null) {
            logger.error("Compiling Core failed. Output directory `" + sketch.getBuildRootPath(target) + File.separator
					+ "core` does not exist or cannot be read or written");
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
