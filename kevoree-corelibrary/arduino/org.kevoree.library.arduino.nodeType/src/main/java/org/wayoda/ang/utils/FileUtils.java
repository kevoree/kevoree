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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Just a collection of static methods for file selection, copying etc...
 */
public class FileUtils {

    private FileUtils() {

    }

    /**
     * Copies a plain file to a new location. If the destination
     * location for the file already exists it will be overwritten
     * @param src the file to be copied
     * @param dest the new fiel to be created or overwritten 
     * @throws IOException if the copy process fails.
     */
    public static void copyFile( File src, File dest ) throws IOException {

        copyFile( src, dest, true );
    }

    /**
     * Copies a plain file to a new location.
     * @param src the file to be copied
     * @param dest the new fiel to be created or overwritten 
     * @param overwrite if true an existing destination file will be overwritten, if
     * false we throw an IOException. 
     * @throws IOException if the copy process fails.
     */
    public static void copyFile( File src, File dest, boolean overwrite ) throws IOException {

        if ( src == null )
            throw new IOException( "Source file is null" );
        if ( dest == null )
            throw new IOException( "Destination file is null" );
        if ( dest.exists() && dest.isFile() && overwrite == false )
            throw new IOException( "Destination file `" + dest.getPath() + "` already exist" );
        if ( !src.isFile() )
            throw new IOException( "Source file `" + src.getPath() + "` is not a plain file" );
        if ( dest.equals( src ) ) {
            /* well, this OK isn't it? */
            return;
        }
        try {
            InputStream in = new FileInputStream( src );
            OutputStream out = new FileOutputStream( dest );
            byte[] buf = new byte[1024];
            int len;
            while (( len = in.read( buf ) ) > 0) {
                out.write( buf, 0, len );
            }
            in.close();
            out.close();
        }
        catch (IOException ioe) {
            throw new IOException( "Error while copying `" + src.getPath() + "` to `" + dest.getPath() + ". " + ioe.getMessage() );
        }
    }

    /**
     * Returns a List of the files in a directory that are accepted by a 
     * FileFilter.
     * @param dir the directory to scan
     * @param fs the FileSelector to apply
     * @return ArrayList<File> The list of File objects, that match
     * the filter. If no files match the list, or the directory 
     * does not exist, or the directory cannot be read we return an empty list.
     */
    public static ArrayList<File> getFiles( File dir, FileSelector fs ) {

        ArrayList<File> retval = new ArrayList<File>();
        if ( dir == null || !dir.exists() || !dir.canRead() ) {
            return retval;
        }
        File[] f = dir.listFiles( fs );
        if ( f != null ) {
            for (int i = 0; i < f.length; i++) {
                retval.add( f[i] );
            }
        }
        return retval;
    }

    /**
     * Recursivly deletes all files and directories from a directory-tree. 
     * On Linux and Mac this method will <strong>not</strong> follow symbolic 
     * links into directories which are not branches of the current tree.
     * @param root the root of the directory-tree.
     * @return boolean true if all files and directories have been removed 
     * succcessfuly.
     */
    public static boolean deleteTree( File root ) {

        if ( root == null || !root.exists() ) {
            //we did not delete anything but its actually gone so why not 
            return true;
        }
        if ( !root.isDirectory() ) {
            //we work only on directories
            return false;
        }
        try {
            String rootPath = root.getCanonicalPath();
            File[] fl = root.listFiles();
            for (int i = 0; i < fl.length; i++) {
                if ( fl[i].getName().equals( "." ) || fl[i].getName().equals( ".." ) )
                    continue;
                if ( fl[i].isFile() ) {
                    if ( !fl[i].delete() ) {
                        //we failed on this one 
                        return false;
                    }
                }
                else {
                    /*
                      We are looking at a directory now.
                      On linux and the Mac this could be a symbolic link to
                      a dir outside this tree. We check this and stop
                      if following the link would kick us out of the 
                      currnet directory tree.
                    */
                    String dirPath = fl[i].getCanonicalPath();
                    if ( !dirPath.startsWith( rootPath ) ) {
                        /*
                          This is a symbolic link pointing 
                          to a directory which is not a subdir
                          of the current one
                        */
                        return false;
                    }
                    //recurse into directory
                    if ( !deleteTree( fl[i] ) ) {
                        //something went wrong one dir down
                        return false;
                    }
                }
            }
        }
        catch (IOException ioe) {
            System.out.println( ioe );
            return false;
        }
        return root.delete();
    }
}
