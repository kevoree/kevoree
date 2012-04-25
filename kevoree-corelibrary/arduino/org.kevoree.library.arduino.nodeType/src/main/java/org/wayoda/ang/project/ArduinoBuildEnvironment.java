/**
 * The MIT License for the "Ang Build Tool"
 * 
 * Copyright (c) <2009> Eberhard Fahle <e.fahle@wayoda.org>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package org.wayoda.ang.project;

import org.kevoree.api.Bootstraper;
import org.kevoree.library.arduinoNodeType.util.ArduinoResourceHelper;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A class for all the information we need about the Arduino build environment
 * which we need to compile the sketches. <br/>
 * Things we need to know are for instance the absolute location of the 
 * core-source files, the bootloaders and the target configuration files. 
 * The class creates only a single(ton) instance of Environment.
 */
public class ArduinoBuildEnvironment {

    /** The name of the Arduino config file for board-settings */
    public static final String ARDUINO_BOARDS_CONFIG = "boards.txt";
    /** The name of the Arduino config file for programmers */
    public static final String ARDUINO_PROGRAMMERS_CONFIG = "programmers.txt";

    /** A Hashtable with all the directories we are going to use */
    private Hashtable<String, File> pathTb;
    /** A list of the default targets defined in targets.conf */
    private TargetList tl = null;
    /** Mutex for synchronizing access to the singleton */
    private static final Object mutex = new Object();

    /** 
     * The private Constructor prevents that we have more than 
     * one instance of this class.
     * @param root the root dir of the Arduino-installation we use for 
     * building the sketches 
     */
    private ArduinoBuildEnvironment(File root) {

        pathTb = new Hashtable<String, File>();

        //populate the hashtable with all the paths we need
        pathTb.put( "root", root );
        pathTb.put( "hardware", new File( root, "hardware" ) );
        pathTb.put( "bootloaders", new File( root, "hardware" + File.separator + "arduino" + File.separator + "bootloaders" ) );
        pathTb.put( "cores", new File( root, "hardware" + File.separator + "arduino" + File.separator + "cores" ) );
        pathTb.put( "libraries", new File( root, "libraries" ) );
        pathTb.put( ARDUINO_BOARDS_CONFIG, new File( root, "hardware" + File.separator + "arduino" + File.separator + ARDUINO_BOARDS_CONFIG ) );
        pathTb.put( ARDUINO_PROGRAMMERS_CONFIG, new File( root, "hardware" + File.separator + "arduino" + File.separator + ARDUINO_PROGRAMMERS_CONFIG ) );
        //check the availability and permissions on all the paths (read-only is fine)
        Enumeration<File> e = pathTb.elements();
        while (e.hasMoreElements()) {
            File f = e.nextElement();
            if ( f != null && f.exists() && f.canRead() ) {
                continue;
            }
            else {
                System.err.println("Required  path `" + f + "` does not exist or cannot be read");
                //JoJoMojo.getMojo().getLog().error( "Required  path `" + f + "` does not exist or cannot be read" );
            }
        }
    }

    /**
     * Returns the single instance of this class. If anything goes wrong
     * during the setup of the Environment we return null.
     * You can use method getStartUpException() to retrieve an expetion
     * that knows more about the reason why we failed. The constructor 
     * in class org.wayoda.ang.applications.Ang could be used as an expamle how to
     * recover from an error in the environment.
     * @return Environment the build-environment in which the process is 
     * running or null if there was an error in setting up the build-environment.
     */
    public static ArduinoBuildEnvironment getInstance() {

        synchronized (mutex) {
            File root = new File( ArduinoResourceHelper.getArduinoHome() );
            return new ArduinoBuildEnvironment( root );
        }
    }

    /**
     * Gets the root-directory with the bootloader code
     * for the arduino boards. 
     * @return File the root directory of the bootloaders
     */
    public File getBootloaderDirectory() {

        return pathTb.get( "bootloaders" );
    }

    /**
     * Gets the root-directory of the cores.
     * @return File the root directory of the cores.
     */
    public File getCoreDirectory() {

        return pathTb.get( "cores" );
    }

    /**
     * Gets the root-directory of the standard libraries.
     * @return File the root directory of the standard libraries.
     */
    public File getStandardLibraryDirectory() {

        return pathTb.get( "libraries" );
    }

    /**
     * Gets a config-file from the Ardino build-environment.
     * @param fn the name of the file to be requested.
     * @return File the config file. Returns null if the
     * file does not exist, is not a plain text-file or 
     * cannont be read.
     */
    public File getConfigFile( String fn ) {

        File f = pathTb.get( fn );
        if ( f != null && f.exists() && f.canRead() ) {
            return f;
        }
        return null;
    }

    /**
     * Gets a list of the default Arduino Targets that are supported 
     * by this software.
     * @return TargetList the list of targets supported 
     * in this build environment. Returns null if the 
     * file with the default targets is not found, or cannot be read.
     */
    public TargetList getDefaultTargetList() {

        File f = null;
        if ( tl == null ) {
            //read the default target list entries
            try {
                f = getConfigFile( ARDUINO_BOARDS_CONFIG );
                tl = new TargetList( f );
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException( "Unable to read default target-list from file `" + f + "`" );
            }
        }
        return tl;
    }
}
