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
import java.util.Iterator;

import org.wayoda.ang.utils.FileSelector;
import org.wayoda.ang.utils.FileUtils;

/**
 * Class for an Arduino standard- or user-library. 
 */
public class Library {

    /** the home-directory of the library*/
    private File libDir;

    /** 
     * The arduino IDE allows a single directory named 'utility' inside
     * the libraries root-dir to contain some more source files.
     */
    private File utilDir;

    /**
     * Creates an Instance of class Library from a directory which is supposed
     * to contain the sourcecode for the library. We don't have many clues 
     * to identify a library on a filesystem. The only clue we have is :<br/>
     * The directory we are looking at in the constructor must contain a header
     * file with the same name as the directory. Apart from that the directory
     * must exist and we have the permisiions to read and write. 
     * The constructor is package-private because only the 
     * {@link org.wayoda.ang.libraries.CodeManager} is allowed to create Cores.
     * @param libDir the directory that is supposed to contain a library.
     * @throws FileNotFoundException if the directory does not exist 
     * or if it does not contain a header-file that matches hee libraries name.
     * @throws SecurityException if the  calling process does not have the permissions
     * to read/write and create files in the source directory.
     */
    Library(File libDir) throws FileNotFoundException {

        boolean headerExists = false;
        if ( libDir == null || !libDir.exists() || !libDir.isDirectory() ) {
            throw new FileNotFoundException( "Library directory not found" );
        }
        else if ( !libDir.canRead() ) {
            throw new IllegalArgumentException( "Can't read files from the libraries source dir" );
        }
        this.libDir = libDir;
        ArrayList<File> headers = getHeaderFiles();
        Iterator<File> it = headers.iterator();
        while (!headerExists && it.hasNext()) {
            File f = it.next();
            String n = f.getName().substring( 0, f.getName().lastIndexOf( "." ) );
            if ( n.equals( libDir.getName() ) ) {
                headerExists = true;
            }

        }
        if ( !headerExists ) {
            throw new FileNotFoundException( "Library header not found" );
        }
        this.utilDir = getUtilDirectory();
    }

    /**
     * Gets the home-directory of the library 
     * @return File the file-object of the library's directory
     */
    public File getDirectory() {

        return libDir;
    }

    /**
     * Gets the name of the library
     * @return String the name of the library which is equal to 
     * the name of the library directory.
     */
    public String getName() {

        return libDir.getName();
    }

    /**
     * Gets the utility directory for source-files if 
     * it exists. An arduino library source directory allows a
     * single subdir that may also contain source code. It must
     * be named 'utilities' (case doesn't matter). Files in this directory
     * will be compiled along with the source files in the library directory itself.
     * @return File the utility directory for the library or null if it doesn't
     * exist.
     */
    public File getUtilDirectory() {

        File uDir = null;
        //search for a dir named toLowerCase(utility)
        File dirs[] = libDir.listFiles( new FileSelector.UtilDirFilter() );
        if ( dirs.length == 1 )
            uDir = dirs[0];
        return uDir;
    }

    /**
     * Scans the library directory for C-Header files. Headers
     * in the utility-dir are ommitted they are for private use only
     * @return ArrayList<File> A list of File objects, which may be empty if       
     * no matching files where found or an IO-Error was raised when scanning for the files.
     */
    public ArrayList<File> getHeaderFiles() {

        return FileUtils.getFiles( libDir, new FileSelector.SourceHeaderFilter() );
    }

    /**
     * Scans the library directory for any kind of C or C++ source files 
     * (files which carry the extension *.c or *.cpp)
     * @return ArrayList<File> A list of File objects, which may be empty if       
     * no matching files where found for this core. If forCompile is true
     * the list will only contain files which require a recompilation.
     */
    public ArrayList<File> getSourceFiles() {

        ArrayList<File> retval = FileUtils.getFiles( libDir, new FileSelector.SourceFileFilter() );
        retval.addAll( FileUtils.getFiles( utilDir, new FileSelector.SourceFileFilter() ) );
        return retval;
    }
}
