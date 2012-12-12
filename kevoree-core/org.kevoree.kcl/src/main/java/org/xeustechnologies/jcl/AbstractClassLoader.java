/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstract class loader that can load classes from different resources
 * 
 * @author Kamran Zafar
 * 
 */
@SuppressWarnings("unchecked")
public abstract class AbstractClassLoader extends ClassLoader {

    protected final List<ProxyClassLoader> loaders = new ArrayList<ProxyClassLoader>();
    private final ProxyClassLoader systemLoader = new SystemLoader();
    private final ProxyClassLoader parentLoader = new ParentLoader();
    private final ProxyClassLoader currentLoader = new CurrentLoader();
   // private final ProxyClassLoader threadLoader = new ThreadContextLoader();
    //private final ProxyClassLoader osgiBootLoader = new OsgiBootLoader();

    /**
     * No arguments constructor
     */
    public AbstractClassLoader() {
        loaders.add( systemLoader );
        loaders.add( parentLoader );
        loaders.add( currentLoader );
        //loaders.add( threadLoader );
    }
    

    public void addLoader(ProxyClassLoader loader) {
        loaders.add( loader );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        System.out.println("!!!!!!!!!!!!!! KCL Hell !!!!!");
        return ( loadClass( className, true ) );
    }

    /**
     * Overrides the loadClass method to load classes from other resources,
     * JarClassLoader is the only subclass in this project that loads classes
     * from jar files
     * 
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     */
    @Override
    public Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        if (className == null || className.trim().equals( "" ))
            return null;

        Collections.sort( loaders );

        Class clazz = null;

        // Check osgi boot delegation
       // if (osgiBootLoader.isEnabled()) {
       //     clazz = osgiBootLoader.loadClass( className, resolveIt );
       // }

        if (clazz == null) {
            for (ProxyClassLoader l : loaders) {
                if (l.isEnabled()) {
                    clazz = l.loadClass( className, resolveIt );
                    if (clazz != null)
                        break;
                }
            }
        }
        return clazz;
    }

    /**
     * Overrides the getResourceAsStream method to load non-class resources from
     * other sources, JarClassLoader is the only subclass in this project that
     * loads non-class resources from jar files
     * 
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        if (name == null || name.trim().equals( "" ))
            return null;

        Collections.sort( loaders );

        InputStream is = null;

        // Check osgi boot delegation
        /*
        if (osgiBootLoader.isEnabled()) {
            is = osgiBootLoader.loadResource( name );
        }*/

        if (is == null) {
            for (ProxyClassLoader l : loaders) {
                if (l.isEnabled()) {
                    is = l.loadResource( name );
                    if (is != null)
                        break;
                }
            }
        }

        return is;

    }

    /**
     * System class loader
     * 
     */
    class SystemLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger( SystemLoader.class.getName() );

        public SystemLoader() {
            order = 5;
            enabled = Configuration.isSystemLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = findSystemClass( className );
            } catch (ClassNotFoundException e) {
                return null;
            }

            //System.out.println("Returning system class " + className);
            /*if (logger.isLoggable( Level.FINEST ))
                logger.finest( "Returning system class " + className );
*/
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getSystemResourceAsStream( name );

            if (is != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( "Returning system resource " + name );

                return is;
            }

            return null;
        }
    }

    /**
     * Parent class loader
     * 
     */
    class ParentLoader extends ProxyClassLoader {
        private final Logger logger = Logger.getLogger( ParentLoader.class.getName() );

        public ParentLoader() {
            order = 3;
            enabled = Configuration.isParentLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = getParent().loadClass( className );
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable( Level.FINEST ))
                logger.finest( "Returning class " + className + " loaded with parent classloader" );

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getParent().getResourceAsStream( name );

            if (is != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( "Returning resource " + name + " loaded with parent classloader" );

                return is;
            }
            return null;
        }

    }

    /**
     * Current class loader
     * 
     */
    class CurrentLoader extends ProxyClassLoader {
        private final Logger logger = Logger.getLogger( CurrentLoader.class.getName() );

        public CurrentLoader() {
            order = 2;
            enabled = Configuration.isCurrentLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = getClass().getClassLoader().loadClass( className );
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable( Level.FINEST ))
                logger.finest( "Returning class " + className + " loaded with current classloader" );

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getClass().getClassLoader().getResourceAsStream( name );

            if (is != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( "Returning resource " + name + " loaded with current classloader" );

                return is;
            }

            return null;
        }

    }

    /**
     * Current class loader
     * 
     */
    class ThreadContextLoader extends ProxyClassLoader {
        private final Logger logger = Logger.getLogger( ThreadContextLoader.class.getName() );

        public ThreadContextLoader() {
            order = 4;
            enabled = Configuration.isThreadContextLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;
            try {
                result = Thread.currentThread().getContextClassLoader().loadClass( className );
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable( Level.FINEST ))
                logger.finest( "Returning class " + className + " loaded with thread context classloader" );

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( name );

            if (is != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( "Returning resource " + name + " loaded with thread context classloader" );

                return is;
            }

            return null;
        }

    }


    public ProxyClassLoader getSystemLoader() {
        return systemLoader;
    }

    public ProxyClassLoader getParentLoader() {
        return parentLoader;
    }

    public ProxyClassLoader getCurrentLoader() {
        return currentLoader;
    }

    public ProxyClassLoader getThreadLoader() {
        return currentLoader;
    }
/*
    public ProxyClassLoader getOsgiBootLoader() {
        return osgiBootLoader;
    }*/
}
