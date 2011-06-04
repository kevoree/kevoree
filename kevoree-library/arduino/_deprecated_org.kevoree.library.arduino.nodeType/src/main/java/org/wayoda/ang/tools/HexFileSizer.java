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

package org.wayoda.ang.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class HexFileSizer implements static methods to calculate
 * the number of databytes stored in file in intel-hex format.
 */
public class HexFileSizer {

    /**
     * We don't need any instances of this class
     * so the constructor is private.
     */
    private HexFileSizer() {

    }

    /**
     * Gets the number of databytes in a intel-hex encoded file
     * @param f the file to be scanned 
     * @return int the number of databytes in the file
     * @throws FileNotFoundException if the file is null, does not exist,
     * cannot be read or is a directory rather than a plain file.
     * @throws Exception if there was an error reading or parsing the contents
     * of the file.
     */
    public static int getSize( File f ) throws Exception {

        if ( f == null ) {
            throw new FileNotFoundException( "File is null" );
        }
        else if ( !f.exists() ) {
            throw new FileNotFoundException( "File '" + f.getPath() + "' does not exist" );
        }
        else if ( !f.canRead() ) {
            throw new FileNotFoundException( "File '" + f.getPath() + "' cannot be read" );
        }
        else if ( !f.isFile() ) {
            throw new FileNotFoundException( "File '" + f.getPath() + "' is not a plain data file" );
        }
        return loadAndCalc( f );
    }

    private static int loadAndCalc( File f ) throws Exception {

        int datasize;
        boolean foundEndOfFile;
        ArrayList<String> sl = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader( new FileReader( f ) );
            String sourceLine;
            while (( sourceLine = in.readLine() ) != null) {
                sl.add( sourceLine );
            }
            in.close();
        }
        catch (IOException ioe) {
            throw new Exception( "IO-Error while reading hex-file '" + f.getName() + "'" );
        }
        foundEndOfFile = false;
        datasize = 0;
        for (int i = 0; i < sl.size(); i++) {
            String s = sl.get( i );
            if ( s.charAt( 0 ) != ':' ) {
                throw new Exception( "Parse error in hex-file '" + f.getName() + "' " + "Line " + ( i + 1 ) + " does not start with ':'" );
            }
            String recType = s.substring( 7, 9 );
            if ( recType.equals( "00" ) ) {
                try {
                    datasize += Integer.parseInt( s.substring( 1, 3 ), 16 );
                }
                catch (NumberFormatException nfe) {
                    throw new Exception( "Parse error in hex-file '" + f.getName() + "' " + "Invalid data size on line " + ( i + 1 ) );
                }
            }
            else if ( recType.equals( "01" ) ) {
                if ( !foundEndOfFile )
                    foundEndOfFile = true;
                else {
                    //two rend of file marks is illegal
                    throw new Exception( "Parse error in hex-file '" + f.getName() + "' " + "End of file mark is repeated on line " + ( i + 1 ) );
                }
            }
        }
        return datasize;
    }
}
