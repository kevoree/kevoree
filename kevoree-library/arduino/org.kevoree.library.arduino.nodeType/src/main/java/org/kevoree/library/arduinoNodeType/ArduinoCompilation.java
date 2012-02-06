/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType;

import org.kevoree.library.arduinoNodeType.util.ArduinoResourceHelper;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wayoda.ang.libraries.Core;
import org.wayoda.ang.libraries.Library;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ffouquet
 */
public class ArduinoCompilation {
    private static final Logger logger = LoggerFactory.getLogger(ArduinoCompilation.class);


    private List<String> cCmd;
    private List<String> cppCmd;

    public void prepareCommands() {

        cCmd = new ArrayList<String>();
        cCmd.add(ArduinoToolChainExecutables.getAVR_GCC());
        cCmd.add("-c");
        cCmd.add("-g");
        cCmd.add("-Os");
        cCmd.add("-w");
        cCmd.add("-ffunction-sections");
        cCmd.add("-fdata-sections");

        cppCmd = new ArrayList<String>();
        cppCmd.add(ArduinoToolChainExecutables.getAVR_GPP());
        cppCmd.add("-c");
        cppCmd.add("-g");
        cppCmd.add("-Os");
        cppCmd.add("-w");
        cppCmd.add("-fno-exceptions");
        cppCmd.add("-ffunction-sections");
        cppCmd.add("-fdata-sections");
    }

    /**
     * Compiles the core source-files for a sketch and a specific target.
     *
     * @param sketch the sketch for which the core is to be compiled.
     * @param target the hardware target for which the core is to be compiled
     * @param core   the core to be compiled
     * @return boolean True if no errors where detected during the compilation
     */
    public void compileCore(Sketch sketch, Target target, Core core) {

        //the command that will be run for compilation
        List<String> cmd = null;

        //check that the build directory for the core exists
        File outputDir = sketch.getCoreBuildRoot(target);
        if (outputDir == null) {
            logger.error("Compiling Core failed. Output directory `" + sketch.getBuildRootPath(target) + File.separator
                    + "core` does not exist or cannot be read or written");
            throw new IllegalStateException();
        }

        List<String> baseC = new ArrayList<String>(cCmd);
        baseC.add("-mmcu=" + target.getMCU());
        baseC.add("-DF_CPU=" + target.getSpeed());
        baseC.add("-I" + core.getDirectory().getPath());

        List<String> baseCPP = new ArrayList<String>(cppCmd);
        baseCPP.add("-mmcu=" + target.getMCU());
        baseCPP.add("-DF_CPU=" + target.getSpeed());
        baseCPP.add("-I" + core.getDirectory().getPath());


        //		String path;
        try {
            List<String> paths = ArduinoResourceHelper.getIncludePaths();
            for (String path : paths) {
                baseCPP.add("-I" + path);
                baseC.add("-I" + path);
            }
        } catch (Exception e) {
//			e.printStackTrace();
        }

        for (File f : core.getSourceFiles()) {
            if (f.getName().toLowerCase().endsWith(".c")) {
                cmd = new ArrayList<String>(baseC);
            } else if (f.getName().toLowerCase().endsWith(".cpp")) {
                cmd = new ArrayList<String>(baseCPP);
            }
            cmd.add(f.getPath());
            cmd.add("-o" + outputDir.getPath() + File.separator + f.getName() + ".o");
            execute(cmd);
        }
        logger.info(
                "Compiled Core `" + target.getCore() + "` target `" + target.getKey() + " = " + target.getName() + "`");
    }

    /**
     * Compiles a libary used by a Sketch for a specific target.
     *
     * @param sketch  the sketch for which the library is to be compiled.
     * @param target  the hardware target for which the library is to be compiled
     * @param library the library to compile
     * @param core    the core files for the platform
     * @return boolean True if no errors where detected during the compilation
     */
    public void compileLibrary(Sketch sketch, Target target, Library library, Core core) {

        File coreSrcDir = core.getDirectory();
        if (!coreSrcDir.exists()) {
            logger.error("Compile library `" + library.getName() + "` failed. Target `" + target.getKey()
                    + "` depends on unknown Core `" + target.getCore() + "`");
            throw new IllegalStateException();
        }

        File outputDir = sketch.getLibraryBuildRoot(target, library);
        if (outputDir == null) {
            logger.error(
                    "Compile library `" + library.getName() + "` failed. Can find build directory for target `" + target
                            .getKey() + "`");
            throw new IllegalStateException();
        }

        /* build the list of directories  to be searched
                     for headers referenced from our code */
        List<String> includePathList = new ArrayList<String>();
        includePathList.add("-I" + library.getDirectory().getPath());
        if (library.getUtilDirectory() != null) {
            includePathList.add("-I" + library.getUtilDirectory().getPath());
        }
        includePathList.add("-I" + coreSrcDir.getPath());

        try {
            List<String> paths = ArduinoResourceHelper.getIncludePaths();
            for (String path : paths) {
                includePathList.add("-I" + path);
            }
        } catch (Exception e) {
//			e.printStackTrace();
        }


        /* The compiler cmdLine for C code */
        List<String> baseC = new ArrayList<String>(cCmd);
        baseC.add("-mmcu=" + target.getMCU());
        baseC.add("-DF_CPU=" + target.getSpeed());
        baseC.addAll(includePathList);

        /* The compiler cmdLine for C++ code */
        List<String> baseCPP = new ArrayList<String>(cppCmd);
        baseCPP.add("-mmcu=" + target.getMCU());
        baseCPP.add("-DF_CPU=" + target.getSpeed());
        baseCPP.addAll(includePathList);

        for (File f : library.getSourceFiles()) {
            List<String> cmd;
            if (f.getName().toLowerCase().endsWith(".c")) {
                cmd = new ArrayList<String>(baseC);
            } else if (f.getName().toLowerCase().endsWith(".cpp")) {
                cmd = new ArrayList<String>(baseCPP);
            } else {
                continue;
            }
            cmd.add(f.getPath());
            cmd.add("-o" + outputDir.getPath() + File.separator + f.getName() + ".o");
            execute(cmd);
        }

        logger.info("Compiled Library `" + library.getName() + "` target `" + target.getKey() + " = " + target.getName()
                + "`");
    }


    /**
     * Compiles a sketch for a specific target.
     *
     * @param sketch the sketch to be compiled.
     * @param target the hardware target for which the files are to be compiled
     * @return boolean True if no errors where detected during the compilation
     */
    public void compileSketch(Sketch sketch, Target target, Core core, ArduinoGenerator generator) {

        /* Compile the C/C++ code generated from the users Arduino-code */
        ArrayList<String> includePathList = new ArrayList<String>();

        /* Add the core-source dir to the list of directories
                     to be searched for header-files */
        includePathList.add("-I" + core.getDirectory().getPath());

        /* Add the source directory of every library referenced from our code */


        File libTempFile = null;
        try {
            libTempFile = File.createTempFile("kevlib", "kevlib");
            libTempFile.delete();
            libTempFile.mkdirs();
            includePathList.add("-I" + libTempFile.getAbsolutePath());
            for (String key : generator.getLibraryKeys()) {
                if (key.endsWith(".h")) {
                    //COPY
                    String cleanFileName = key;
                    if (cleanFileName.contains(File.separator)) {
                        cleanFileName = cleanFileName.substring(cleanFileName.lastIndexOf(File.separator) + 1);
                    }
                    File outFile = new File(libTempFile + File.separator + cleanFileName);
                    ArduinoResourceHelper.copyInputStream(generator.getLibrary(key), new FileOutputStream(outFile));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Library library : sketch.getLibraries()) {
            includePathList.add("-I" + library.getDirectory().getPath());
            if (library.getUtilDirectory() != null) {
                includePathList.add("-I" + library.getUtilDirectory().getPath());
            }
        }
        //		String path;
        try {
            List<String> paths = ArduinoResourceHelper.getIncludePaths();
            for (String path : paths) {
                includePathList.add("-I" + path);
            }
        } catch (Exception e) {
//			e.printStackTrace();
        }

        /* Add the target-dir of the Sketch too */
        includePathList.add("-I" + sketch.getBuildRootPath(target));

        /* The compiler cmdLine for C code */
        ArrayList<String> baseC = new ArrayList<String>(cCmd);
        baseC.add("-mmcu=" + target.getMCU());
        baseC.add("-DF_CPU=" + target.getSpeed());
        baseC.addAll(includePathList);

        /* The compiler cmdLine for C++ code */
        ArrayList<String> baseCPP = new ArrayList<String>(cppCmd);
        baseCPP.add("-mmcu=" + target.getMCU());
        baseCPP.add("-DF_CPU=" + target.getSpeed());
        baseCPP.addAll(includePathList);


        for (String key : generator.getLibraryKeys()) {
            if (key.endsWith(".cpp") || key.endsWith(".c")) {
                ArrayList<String> cmd;
                if (key.toLowerCase().endsWith(".c")) {
                    cmd = new ArrayList<String>(baseC);
                } else if (key.toLowerCase().endsWith(".cpp")) {
                    cmd = new ArrayList<String>(baseCPP);
                } else {
                    //here we have some other type of file we don't have to compile
                    continue;
                }
                //COPY FILE
                try {
                    String cleanFileName = key;
                    if (cleanFileName.contains(File.separator)) {
                        cleanFileName = cleanFileName.substring(cleanFileName.lastIndexOf(File.separator) + 1);
                    }
                    File outFileCpp = new File(libTempFile + File.separator + cleanFileName);
                   // System.out.println(outFileCpp.getAbsolutePath());
                    ArduinoResourceHelper.copyInputStream(generator.getLibrary(key), new FileOutputStream(outFileCpp));

                    cmd.add(outFileCpp.getAbsolutePath());
                    cmd.add("-o");
                    cmd.add(sketch.getBuildRootPath(target) + File.separator + cleanFileName + ".o");
                    execute(cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


        for (File file : sketch.getSourceFiles(target)) {

            ArrayList<String> cmd;

            if (file.getName().toLowerCase().endsWith(".c")) {
                cmd = new ArrayList<String>(baseC);
            } else if (file.getName().toLowerCase().endsWith(".cpp")) {
                cmd = new ArrayList<String>(baseCPP);
            } else {
                //here we have some other type of file we don't have to compile
                continue;
            }

            cmd.add(file.getPath());
            cmd.add("-o");
            cmd.add(sketch.getBuildRootPath(target) + File.separator + file.getName() + ".o");
            execute(cmd);
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
