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

package org.wayoda.ang.utils;

import java.io.File;
import java.util.ArrayList;

import org.wayoda.ang.project.ArduinoBuildEnvironment;

/**
 * The storage for runtime properties. The preference storage works on a per user
 * base. Since only one set of AngPreferences is needed during the runtime of an
 * application, this class is designed as a singleton.
 */
public class AngPreferences {

    /** a mutex protects the creation of our singleton */
    private static final Object mutex = new Object();
    /** A FileSelector that checks user supplied paths */
    private FileSelector.DirectoryFilter pathFilter = new FileSelector.DirectoryFilter();

    /**
     * Creates the AngPreferences singleton instance. 
     */
    private AngPreferences() {

    }

    /**
     * Returns the AngPreferences singleton instance. 
     * @return AngPreferences the preferences for this software.
     */
    public static AngPreferences getInstance() {

        synchronized (mutex) {
            return new AngPreferences();
        }
    }

    /**
     * Gets a list of all directories that will be searched for libraries
     * included by a Sketch. 
     * @return ArrayList<File> a list of possible library directories
     */
    public ArrayList<File> getLibraryPath() {

        ArrayList<File> lp = new ArrayList<File>();
        String path = System.getProperty( "library.path" );
        if ( path != null ) {
            String[] splitPath = path.split( File.pathSeparator );
            for (int i = 0; i < splitPath.length; i++) {
                File f = new File( splitPath[i] );
                if ( pathFilter.accept( f ) )
                    lp.add( f );
            }
        }
        //add the standard arduino-libs to the end of the list
        ArduinoBuildEnvironment env = ArduinoBuildEnvironment.getInstance();
        if ( env != null )
            lp.add( env.getStandardLibraryDirectory() );
        return lp;
    }
}
