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

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * Provides a more embedder friendly {@link URLClassLoader} for APT. The constructor that takes no parent will delegate
 * to the APT class loader instead of the system class loader.
 *
 * @author Benjamin Bentmann
 */
public class EmbeddedURLClassLoader
    extends URLClassLoader
{

    /**
     * Fixes the class loading in APT to be embeddable. To load a factory, APT is doing something like
     *
     * <pre>
     * ClassLoader classLoader = new URLClassLoader( classPath );
     * AnnotationProcessorFactory factory = (AnnotationProcessorFactory) classLoader.loadClass( name );
     * </pre>
     *
     * i.e. it's assumed that {@code AnnotationProcessorFactory} is present in the system class loader. This is
     * generally wrong so we derive the factory class loader from the APT class loader.
     */
    public EmbeddedURLClassLoader( URL[] urls )
    {
        this( urls, AptClassLoader.INSTANCE );
    }

    public EmbeddedURLClassLoader( URL[] urls, ClassLoader parent )
    {
        super( urls, parent );
    }

    public EmbeddedURLClassLoader( URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory )
    {
        super( urls, parent, factory );
    }

}