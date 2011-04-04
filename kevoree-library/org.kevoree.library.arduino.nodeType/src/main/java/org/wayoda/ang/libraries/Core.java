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

import org.wayoda.ang.utils.FileSelector;
import org.wayoda.ang.utils.FileUtils;

/**
 * Manages the core files for the arduino project. 
 * From the content of the files in the core directory
 * its a bit hard to define what the <em><strong>Core</strong></em>
 * actually is, the simple point of view taken here is taht 
 * the Core is a directory with a lots of C/C++ sourcefiles 
 * that have to be compiled an linked into every Sketch.
 * The constructor of this class is package-private because only the 
 * {@link org.wayoda.ang.libraries.CodeManager} is allowed to create new Cores.
 */
public class Core {

    /** 
     * The standard file name for the core archive that is build by
     * the compiler. Application code should not assume 
     * the actual value of this constant to be constant across versions.
     * Use this constant whenever refering to the name of the archive 
     * puts you on the safe side.
     */
    public static final String CORE_ARCHIVE_NAME = "core.a";

    /** the directory where the sources for the core live */
    private File coreDir = null;

    /**
     * Creates an Instance of class Core. There is nothing in the 
     * Core directory that indentifies itself as a core. So we 
     * accept very directory that exists, can be read and written. 
     * Hopefully the {@link org.wayoda.ang.libraries.CodeManager} never betrays us
     * by calling us with a directory that does not contain a valid core.
     * @param coreDir the directory that is supposed to contain a core.
     * @throws FileNotFoundException if the directory supposed to contain
     * the sourcefiles for the core does not exist.
     * @throws SecurityException if the  calling process does not have the permissions
     * to read/write and create files in the source directory.
     */
    Core(File coreDir) throws FileNotFoundException, SecurityException {

        if ( coreDir == null || !coreDir.exists() ) {
            throw new FileNotFoundException( "Core source directory `" + coreDir.getName() + "` does not exist!" );
        }
        else if ( !coreDir.canRead() ) {
            throw new SecurityException( "Not permitted to read contents of core source directory `" + coreDir.getName() + "`!" );
        }
        else if ( !coreDir.canWrite() ) {
            throw new SecurityException( "Not permitted to write or create files in core source directory `" + coreDir.getPath() + "`!" );
        }
        this.coreDir = coreDir;
    }

    /**
     * Gets the directory where source files for this core are located
     * @return File the directory with the Core's source files 
     */
    public File getDirectory() {

        return coreDir;
    }

    /**
     * Scans the library directory for any kind of C or C++ source files 
     * (files which carry the extension *.c or *.cpp)
     * @return ArrayList<File> A list of File objects, which may be empty if       
     * no matching files where found for this core. If forCompile is true
     * the list will only contain files which require a recompilation.
     */
    public ArrayList<File> getSourceFiles() {

        return FileUtils.getFiles( coreDir, new FileSelector.SourceFileFilter() );
    }
}
