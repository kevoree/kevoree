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
import java.io.IOException;

/**
 * Most parts of the sourcecode for the Ang-project 
 * are always busy finding, moving or deleting files or groups 
 * of files. <br/> To make this a bit easier to code we implement a bunch of 
 * filters for files and directories that can be applied to the source
 * and build directories used in Ang. <br/>
 * Class FileSelector is just the abstract baseclass of the nested classes 
 * that do the filtering. FileSelector implements the java.io.FileFilter
 * interface so any class derived from FileSelector can be used in all places
 * where a plain FileFilter is used.
 */
public abstract class FileSelector implements java.io.FileFilter {

    /** Private constructor is never called */
    private FileSelector() {

    }

    /**
     * Test wether the File f is accepted by this FileSelector.
     * @return boolean true if the file is not null and exists
     */
    public boolean accept( File f ) {

        if ( f != null && f.exists() )
            return true;
        return false;
    }

    /**
     * Filter used for finding the utility-directory in  
     * the source-code directory of a library.
     */
    public static class UtilDirFilter extends FileSelector {

        /** Creates a new Filter */
        public UtilDirFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a directory we can read from
         * and its name equals "utility". 
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isDirectory() && f.canRead() && f.getName().toLowerCase().equals( "utility" ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * Filter used for finding the exmaple-directory in  
     * the source-code directory of a library.
     */
    public static class ExampleDirFilter extends FileSelector {

        /** Creates a new Filter */
        public ExampleDirFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a directory we can read from
         * and its name equals "examples". 
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isDirectory() && f.canRead() && f.getName().toLowerCase().equals( "examples" ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A simple filter for directories.
     * The directory must be readable to pass the filter. 
     */
    public static class DirectoryFilter extends FileSelector {

        /** Creates a new Filter */
        public DirectoryFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a directory we can read from
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isDirectory() && f.canRead() ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A simple filter for <em>normal</em> files.
     * The file directory must be readable to pass the filter. 
     */
    public static class PlainFileFilter extends FileSelector {

        /** Creates a new Filter */
        public PlainFileFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from.
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter object files created by a compiler.
     */
    public static class ObjectFileFilter extends FileSelector {

        /** Creates a new Filter */
        public ObjectFileFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".o".
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && f.getName().toLowerCase().endsWith( ".o" ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter (C/C++) Headers.
     */
    public static class SourceHeaderFilter extends FileSelector {

        /** Creates a new Filter */
        public SourceHeaderFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".h" or ".hpp"
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && ( f.getName().toLowerCase().endsWith( ".h" ) || f.getName().toLowerCase().endsWith( ".hpp" ) ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter for compiled code that can be uploaded to a microcontroller.
     */
    public static class CodeFilter extends FileSelector {

        /** Creates a new Filter */
        public CodeFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".hex" or ".eep"
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && ( f.getName().toLowerCase().endsWith( ".hex" ) || f.getName().toLowerCase().endsWith( ".eep" ) ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter (C/C++) implementation files.
     */
    public static class SourceFileFilter extends FileSelector {

        /** Creates a new Filter */
        public SourceFileFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".c" or ".cpp"
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && ( f.getName().toLowerCase().endsWith( ".c" ) || f.getName().toLowerCase().endsWith( ".cpp" ) ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter (C/C++) source files. Headers and inplementation
     * files.
     */
    public static class AllSourceFilter extends FileSelector {

        /** Creates a new Filter */
        public AllSourceFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".h" ".hpp " ".c" or ".cpp"
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && ( f.getName().toLowerCase().endsWith( ".c" ) || f.getName().toLowerCase().endsWith( ".cpp" ) || f.getName().toLowerCase().endsWith( ".h" ) || f.getName().toLowerCase().endsWith( ".hpp" ) ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter for source files that can appear as part of a sketch.
     */
    public static class SketchSourceFilter extends FileSelector {

        /** Creates a new Filter */
        public SketchSourceFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".pde" ".h" ".hpp " ".c" or ".cpp"
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && ( f.getName().toLowerCase().endsWith( ".pde" ) || f.getName().toLowerCase().endsWith( ".h" ) || f.getName().toLowerCase().endsWith( ".hpp" ) || f.getName().toLowerCase().endsWith( ".c" ) || f.getName().toLowerCase().endsWith( ".cpp" ) ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A filter for pde-source files.
     */
    public static class PdeFilter extends FileSelector {

        /** Creates a new Filter */
        public PdeFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * @param f the file to run through this filter 
         * @return boolean True if f is a file we can read from and its
         * name ends with ".pde" 	 */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isFile() && f.canRead() && f.getName().toLowerCase().endsWith( ".pde" ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * A FileFilter for Sketches. Since a Sketch is a 
     * structured collection of files it must match the following 
     * criteria to pass the filter:<ul>
     * <li>All files that belong to a Sketch are located  in the same
     * directory. </li>
     * <li>The directory must be readable and writeable.</li>
     * <li>Inside the directory there is a file with the same 
     * name as the directory which ends on the extension ".pde".</li>
     * <li>All files in the directory that have one of the extensions 
     * *.pde,*.h,*.c or *.cpp belong to the sketch and must be readable.</li>
     * </ul>
     */
    public static class SketchFilter extends FileSelector {

        /** Creates a new Filter */
        public SketchFilter() {

        }

        /**
         * Test wether the File f is accepted by this FileSelector.
         * There are two different argumnets we accept. If the method 
         * is called with a directory it must match the criteria from the 
         * class description.<br/>
         * If it is called with a plain file with the extension ".pde"
         * we take its parent and see wether it matches the criteria 
         * of a Sketch.
         * @param f the file to run through this filter 
         * @return boolean True if f is Sktech-directory or the 
         * main pde-file of a Sktech.
         */
        public boolean accept( File f ) {

            if ( super.accept( f ) && f.isDirectory() && f.canRead() && f.canWrite() ) {
                /* 
                   if we are called with a directory
                   there must be a pde-file with the same 
                   in here.
                */
                File list[] = f.listFiles( new SketchSourceFilter() );
                for (int i = 0; i < list.length; i++) {
                    String fname = list[i].getName();
                    if ( fname.toLowerCase().endsWith( ".pde" ) ) {
                        fname = fname.substring( 0, fname.lastIndexOf( '.' ) );
                        if ( fname.equals( f.getName() ) )
                            return true;
                    }
                }
            }
            else {
                /* 
                   seems we are called with a plain file, so its name must end
                   with .pde and the parent directory must have the same name
                   without the pde extension and we must have the 
                   permissions to read and write to the directory
                */
                if ( super.accept( f ) && f.isFile() && f.canRead() && ( (String) ( f.getName() ) ).endsWith( ".pde" ) ) {
                    //the name of the sketch without the pde-ending must match the dirname
                    String fname = f.getName().substring( 0, f.getName().lastIndexOf( '.' ) );
                    File parent = null;
                    try {
                        parent = f.getCanonicalFile().getParentFile();
                        if ( fname.equals( parent.getName() ) && parent.canRead() && parent.canWrite() )
                            return true;
                    }
                    catch (IOException ioe) {
                        return false;
                    }
                }
            }
            return false;
        }
    }
}
