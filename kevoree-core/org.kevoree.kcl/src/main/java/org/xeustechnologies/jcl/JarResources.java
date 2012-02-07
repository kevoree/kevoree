/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xeustechnologies.jcl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xeustechnologies.jcl.exception.JclException;

/**
 * JarResources reads jar files and loads the class content/bytes in a HashMap
 * 
 * @author Kamran Zafar
 * 
 */
public class JarResources {

    protected Map<String, byte[]> jarEntryContents;

    protected byte[] getJarEntryContents(String name){
        return jarEntryContents.get(name);
    }

    protected boolean collisionAllowed;

    private static Logger logger = Logger.getLogger( JarResources.class.getName() );

    /**
     * Default constructor
     */
    public JarResources() {
        jarEntryContents = new HashMap<String, byte[]>();
        collisionAllowed = Configuration.suppressCollisionException();
    }

    /**
     * @param name
     * @return byte[]
     */
    public byte[] getResource(String name) {
        return getJarEntryContents(name);
     //   return jarEntryContents.get( name );
    }

    /**
     * Returns an immutable Map of all jar resources
     * 
     * @return Map
     */
    public Map<String, byte[]> getResources() {
        return Collections.unmodifiableMap( jarEntryContents );
    }

    /**
     * Reads the specified jar file
     * 
     * @param jarFile
     */
    public void loadJar(String jarFile) {
        if (logger.isLoggable( Level.FINEST ))
            logger.finest( "Loading jar: " + jarFile );

        FileInputStream fis = null;
        try {
            fis = new FileInputStream( jarFile );
            loadJar( fis );
        } catch (IOException e) {
            throw new JclException( e );
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }
        }
    }

    /**
     * Reads the jar file from a specified URL
     * 
     * @param url
     */
    public void loadJar(URL url) {
        if (logger.isLoggable( Level.FINEST ))
            logger.finest( "Loading jar: " + url.toString() );

        InputStream in = null;
        try {
            in = url.openStream();
            loadJar( in );
        } catch (IOException e) {
            throw new JclException( e );
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }
        }
    }

    /**
     * Load the jar contents from InputStream
     * 
     */
    public void loadJar(InputStream jarStream) {

        BufferedInputStream bis = null;
        JarInputStream jis = null;

        try {
            bis = new BufferedInputStream( jarStream );
            jis = new JarInputStream( bis );

            JarEntry jarEntry = null;
            while (( jarEntry = jis.getNextJarEntry() ) != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( dump( jarEntry ) );

                if (jarEntry.isDirectory()) {
                    continue;
                }

                if (jarEntryContents.containsKey( jarEntry.getName() )) {
                    if (!collisionAllowed)
                        throw new JclException( "Class/Resource " + jarEntry.getName() + " already loaded" );
                    else {
                        if (logger.isLoggable( Level.FINEST ))
                            logger.finest( "Class/Resource " + jarEntry.getName()
                                    + " already loaded; ignoring entry..." );
                        continue;
                    }
                }

                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( "Entry Name: " + jarEntry.getName() + ", " + "Entry Size: " + jarEntry.getSize() );

                byte[] b = new byte[2048];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int len = 0;
                while (( len = jis.read( b ) ) > 0) {
                    out.write( b, 0, len );
                }

                // add to internal resource HashMap
                jarEntryContents.put( jarEntry.getName(), out.toByteArray() );

                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( jarEntry.getName() + ": size=" + out.size() + " ,csize="
                            + jarEntry.getCompressedSize() );

                out.close();
            }
        } catch (IOException e) {
            throw new JclException( e );
        } catch (NullPointerException e) {
            if (logger.isLoggable( Level.FINEST ))
                logger.finest( "Done loading." );
        } finally {
            if (jis != null)
                try {
                    jis.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }

            if (bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }
        }
    }

    /**
     * For debugging
     * 
     * @param je
     * @return String
     */
    private String dump(JarEntry je) {
        StringBuffer sb = new StringBuffer();
        if (je.isDirectory()) {
            sb.append( "d " );
        } else {
            sb.append( "f " );
        }

        if (je.getMethod() == JarEntry.STORED) {
            sb.append( "stored   " );
        } else {
            sb.append( "defalted " );
        }

        sb.append( je.getName() );
        sb.append( "\t" );
        sb.append( "" + je.getSize() );
        if (je.getMethod() == JarEntry.DEFLATED) {
            sb.append( "/" + je.getCompressedSize() );
        }

        return ( sb.toString() );
    }
}
