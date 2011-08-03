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
package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Loads the APT processor class and enables it for embedding. Inspired by the JavacClassLoader from Apache Commons JCI
 * and the Maven Shade Plugin. When loading APT and "com.sun.*" classes in general, we will prefer the plugin class
 * loader as this could know about a system-scope dependency (on {@code tools.jar}) from the POM. If the plugin class
 * loader fails, we will fallback to the {@code tools.jar} of the current JDK.
 *
 * @author Benjamin Bentmann
 */
class AptClassLoader
    extends URLClassLoader
{

    public static final ClassLoader INSTANCE = new AptClassLoader();

    private Map<String, Class<?>> loadedClasses = new HashMap<String, Class<?>>();

    private AptClassLoader()
    {
        super( getClassPath(), null );
    }

    static File getToolsJar()
    {
        return new File( System.getProperty( "java.home" ), "../lib/tools.jar" );
    }

    private static URL[] getClassPath()
    {
        File toolsJar = getToolsJar();
        if ( toolsJar.isFile() )
        {
            try
            {
                return new URL[] { toolsJar.toURI().toURL() };
            }
            catch ( MalformedURLException e )
            {
                // cannot happen
            }
        }
        // no need to panic, could still be on the bootstrap class path or provided via plugin dependency
        return new URL[0];
    }

    @Override
    protected synchronized Class<?> loadClass( String name, boolean resolve )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "com.sun.tools.apt." ) )
        {
            // APT impl uses the URLClassLoader to load factories and such, so inject our tweaked class loader

            Class<?> loadedClass = loadedClasses.get( name );

            if ( loadedClass == null )
            {
                try
                {
                    String classFile = name.replace( '.', '/' ) + ".class";

                    InputStream classStream = getClass().getClassLoader().getResourceAsStream( classFile );

                    if ( classStream == null )
                    {
                        classStream = getResourceAsStream( classFile );
                    }

                    ClassReader classReader = new ClassReader( classStream );

                    ClassWriter classWriter = new ClassWriter( classReader, 0 );

                    ClassVisitor classVisitor = new RemappingClassAdapter( classWriter, new ClassRemapper() );

                    classReader.accept( classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES );

                    byte[] classBytes = classWriter.toByteArray();

                    loadedClass = defineClass( name, classBytes, 0, classBytes.length );

                    loadedClasses.put( name, loadedClass );
                }
                catch ( IOException e )
                {
                    throw new ClassNotFoundException( name, e );
                }
            }

            if ( resolve )
            {
                resolveClass( loadedClass );
            }

            return loadedClass;
        }
        else if ( EmbeddedURLClassLoader.class.getName().equals( name ) )
        {
            // make sure APT can load the tweaked class loader we injected into its code

            return getClass().getClassLoader().loadClass( name );
        }
        else if ( name.startsWith( "com.sun." ) )
        {
            // APT API

            try
            {
                // try plugin class loader first to give priority to explicit plugin dependency
                return getClass().getClassLoader().loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                // now try auto-detected tools.jar of current JDK below
            }
        }

        return super.loadClass( name, resolve );
    }

    static class ClassRemapper
        extends Remapper
    {

        private final String from = URLClassLoader.class.getName().replace( '.', '/' );

        private final String to = EmbeddedURLClassLoader.class.getName().replace( '.', '/' );

        @Override
        public String map( String type )
        {
            if ( from.equals( type ) )
            {
                return to;
            }
            return type;
        }

    }

}