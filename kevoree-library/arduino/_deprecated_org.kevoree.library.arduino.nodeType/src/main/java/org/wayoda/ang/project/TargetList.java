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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

/**
 * The TargetList keeps track of all the different targets for which 
 * Sketches can be compiled. The basic targets are defined in a file 
 * but it is possible to add custom targets at runtime. 
 */
public class TargetList {

    /** the list of the keys for all available targets */
    private Hashtable<String, Target> targets = new Hashtable<String, Target>();

    /**
     * Creates a new instance of class TargetList by reading 
     * the target-properties from a file, and the internal target-config resources
     * @throws FileNotFoundException if the file argument is null, or does not exist
     * or is not a plain file.
     * @throws IOException if there was an error when trying to load the properties file
     * @throws IllegalArgumnetException if we encounter a problem reading the 
     * target definitions.
     */
    public TargetList(File f) throws FileNotFoundException, IOException, Exception {

        if ( f == null )
            throw new FileNotFoundException( "The file supposed to contain the target definitions is null" );
        if ( !f.exists() )
            throw new FileNotFoundException( "The file `" + f.getName() + "` does not exist" );
        if ( !f.isFile() )
            throw new FileNotFoundException( "The file `" + f.getName() + "` is not a normal file" );
        if ( !f.canRead() )
            throw new FileNotFoundException( "The file `" + f.getName() + "` cannot be opend for reading" );
        InputStream is = getClass().getResourceAsStream( "/arduino/isptargets.config" );
        if ( is == null )
            throw new FileNotFoundException( "The internal config file `isptargets.config`" + " was not found" );
        loadTargets( f, is );
    }

    /**
     * Gets a Target from the list.
     * @param key The key for the target that is requested.
     * @return Target The Target for key if it is found in the list, null otherwise.
     */
    public Target getTarget( String key ) {

        return targets.get( key );
    }

    private void loadTargets( File f, InputStream is ) throws Exception {

        Properties arduinoProps;
        Properties ispProps;
        ArduinoBuildEnvironment env = ArduinoBuildEnvironment.getInstance();
        arduinoProps = new Properties();
        try {
            arduinoProps.load( new FileInputStream( f ) );
        }
        catch (IOException ioeArduino) {
            throw new IOException( "Unable to read contents of file " + f.getName() );
        }
        catch (IllegalArgumentException iaeArduino) {
            throw new IOException( "Malformed input in file " + f.getName() );
        }
        ispProps = new Properties();
        try {
            ispProps.load( is );
        }
        catch (IOException ioeInternal) {
            throw new IOException( "Unable to read internal `isptargets.config`" );
        }
        catch (IllegalArgumentException iaeInternal) {
            throw new IOException( "Malformed input in internal `isptargets.config`" );
        }
        //create a list of the keys in the arduino file
        HashSet<String> keySet = new HashSet<String>();
        Enumeration e = arduinoProps.propertyNames();
        while (e.hasMoreElements()) {
            String p = (String) e.nextElement();
            String key = p.substring( 0, p.indexOf( '.' ) );
            if ( key.equals( "" ) || key.equals( p ) ) {
                //invalid for an entry in the properties
                throw new IllegalArgumentException( "Malformed input in file " + f.getName() + " property=\"" + p + "\"" );
            }
            keySet.add( key );
        }
        //now iterate through the keys in the arduino config and create the targets
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String key = it.next();
            //check the path to the bootloader code file 
            String bootloaderPath = arduinoProps.getProperty( key + ".bootloader.path" ) + File.separator + arduinoProps.getProperty( key + ".bootloader.file" );
            File codefile = new File( env.getBootloaderDirectory(), bootloaderPath );
            if ( codefile.exists() && codefile.isFile() && codefile.canRead() ) {
                bootloaderPath = codefile.getPath();
            }
            else {
                System.err.println("Bootloader code for target \"" + key + "\" was not found or cannot" + " be opened for reading.");
               // throw new IllegalArgumentException( "Bootloader code for target \"" + key + "\" was not found or cannot" + " be opened for reading." );
            }
            //create the target  
            targets.put( key, new Target( key, arduinoProps.getProperty( key + ".name" ), arduinoProps.getProperty( key + ".build.mcu" ), arduinoProps.getProperty( key + ".build.f_cpu" ), arduinoProps.getProperty( key + ".build.core" ), arduinoProps.getProperty( key + ".upload.maximum_size" ), arduinoProps.getProperty( key + ".upload.speed" ) ) );
        }
    }
}
