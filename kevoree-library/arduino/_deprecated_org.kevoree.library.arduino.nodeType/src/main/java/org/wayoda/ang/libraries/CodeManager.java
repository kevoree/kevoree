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

package org.wayoda.ang.libraries;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.wayoda.ang.project.ArduinoBuildEnvironment;
import org.wayoda.ang.project.Target;
import org.wayoda.ang.utils.AngPreferences;
import org.wayoda.ang.utils.FileSelector;

/**
 * Manges all the core-/standard-/user-libraries found in the environment. 
 * The class knows where the arduino core and standard libraries
 * are to be found and provides methods to add more cores and libararies
 * at runtime.
 */
public class CodeManager {

    /** the single(ton) instance of a CodeManager we work with */
    private static CodeManager cm = null;
    /** a mutex that protects the creation of the CodeManager */
    private static final Object mutex = new Object();
    /** A Hashtable for the arduino cores */
    private Hashtable<String, Core> cores = new Hashtable<String, Core>();
    /** A list of Hashtables for the standard and user libraries */
    private ArrayList<LibPath> libs = new ArrayList<LibPath>();

    /**
     * Create a CodeManager instance that manages 
     * all the standard Cores and libraries. 
     */
    private CodeManager() {

        AngPreferences ap = AngPreferences.getInstance();
        setLibraryPath( ap.getLibraryPath() );
        ArduinoBuildEnvironment abe = ArduinoBuildEnvironment.getInstance();
        reloadCores( abe.getCoreDirectory() );
    }

    /** 
     * Gets the single instance of CodeManager that is available
     * @return CodeManager the single instance of CodeManager
     */
    public static CodeManager getInstance() {

        synchronized (mutex) {
            if ( cm == null )
                cm = new CodeManager();
            return cm;
        }
    }

    /**
     * Sets the search path for libraries.
     * @param path a list of directories to be searched for libraries
     */
    private void setLibraryPath( ArrayList<File> path ) {

        if ( path != null ) {
            //delete the current search path and build a new one
            libs.clear();
            for (File f : path) {
                try {
                    LibPath lp = new LibPath( f );
                    libs.add( lp );
                }
                catch (FileNotFoundException fnfe) {
                }
            }
        }
    }

    /**
     * Gets the core for a specific target. 
     * While the method for retrieving a library 
     * {@link org.wayoda.ang.libraries.CodeManager#getLibrary(String)}
     * needs the name for by which the library is identified, this is not necessary 
     * here. A {@link org.wayoda.ang.project.Target} always references only one Core 
     * that can be read by {@link org.wayoda.ang.project.Target#getCore()}.
     * @param target the Target for which the core is to be configured. 
     * @return Core the core if it exists. 
     * Null if the core for the target was not found 
     */
    public Core getCore( Target target ) {

        return cores.get( target.getCore() );
    }

    /**
     * Gets a Library.  
     * @param name the name of the Library. 
     * @return Library the library if it exists. 
     * Null if the Library was not found 
     */
    public Library getLibrary( String name ) {

        Library lib = null;
        for (LibPath lp : libs) {
            if ( ( lib = lp.getLibrary( name ) ) != null )
                break;
        }
        return lib;
    }

    /**
     * Scans for cores in a directory.
     * @param coreRoot the root directory to be scanned for cores.
     */
    private void reloadCores( File coreRoot ) {

        FileSelector.DirectoryFilter df = new FileSelector.DirectoryFilter();
        if ( df.accept( coreRoot ) ) {
            File[] coredirs = coreRoot.listFiles( df );
            for (int i = 0; i < coredirs.length; i++) {
                String key = coredirs[i].getName();
                if ( !cores.containsKey( key ) ) {
                    try {
                        Core c = new Core( coredirs[i] );
                        cores.put( key, c );
                    }
                    catch (Exception e) {
                        //If there are dirs which are not cores drop silently 
                    }
                }
            }
        }
    }

    /**
     * A private class the handles all the libraries in a specific 
     * path of the filesystem.
     */
    private class LibPath {

        //the root directory of the library 
        private File root = null;
        //the libraries found in this location
        private Hashtable<String, Library> ht = new Hashtable<String, Library>();

        /**
         * Creates a new LibPath object.
         * @param root the directory we scan for libraries
         * @throws FileNotFoundException if the root-directory does not exist
         * or cannot be read. 
         */
        public LibPath(File root) throws FileNotFoundException {

            if ( root == null )
                throw new FileNotFoundException( "Library path is null" );
            if ( root == null || !root.exists() )
                throw new FileNotFoundException( "Library path `" + root.getPath() + "` does not exist" );
            if ( !root.canRead() )
                throw new FileNotFoundException( "Library path `" + root.getPath() + "` cannot be opened for reading" );
            this.root = root;
            reload();
        }

        /**
         * Gets a Library if it exists in this path
         * @param name the name of the library we are looking for
         * @return Library the Library if it exists
         */
        public Library getLibrary( String name ) {

            return ht.get( name );
        }

        /**
         * Scans the directory for libraries
         */
        private void reload() {

            //forget about old entries
            ht.clear();
            if ( root == null || !root.exists() || !root.isDirectory() || !root.canRead() ) {
                /* if anything bad happened to the root in the meantime
                   we stop right away. */
                return;
            }
            /* Scan for any new libraries inside the root diretory */
            File[] ldirs = root.listFiles( new FileSelector.DirectoryFilter() );
            for (int i = 0; i < ldirs.length; i++) {
                String key = ldirs[i].getName();
                try {
                    Library lib = new Library( ldirs[i] );
                    ht.put( key, lib );
                }
                catch (Exception e) {
                    //its ok if there are directories which are not libraries
                }
            }
        }

    }
}
