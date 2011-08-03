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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;

/**
 * Provides utilities for working with Mojo logs.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: LogUtils.java 7031 2008-05-22 09:41:31Z mark $
 */
public final class LogUtils
{
    // constants --------------------------------------------------------------

    public static final int LEVEL_DEBUG = 0;

    public static final int LEVEL_INFO = 1;

    public static final int LEVEL_WARN = 2;

    public static final int LEVEL_ERROR = 3;

    // constructors -----------------------------------------------------------

    private LogUtils()
    {
        throw new AssertionError();
    }

    // public methods ---------------------------------------------------------

    public static void log( Log log, int level, File file )
    {
        log( log, level, file, null );
    }

    public static void log( Log log, int level, File file, CharSequence prefix )
    {
        if ( !isEnabled( log, level ) )
        {
            return;
        }

        FileReader reader = null;

        try
        {
            reader = new FileReader( file );

            log( log, level, reader, prefix );
        }
        catch ( FileNotFoundException exception )
        {
            log.warn( "Error logging file", exception );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    public static void log( Log log, int level, Reader reader )
    {
        log( log, level, reader, null );
    }

    public static void log( Log log, int level, Reader reader, CharSequence prefix )
    {
        if ( !isEnabled( log, level ) )
        {
            return;
        }

        BufferedReader bufferedReader = new BufferedReader( reader );

        String line;

        try
        {
            while ( ( line = bufferedReader.readLine() ) != null )
            {
                log( log, level, line, prefix );
            }
        }
        catch ( IOException exception )
        {
            log.warn( "Error logging reader", exception );
        }
    }

    public static void log( Log log, int level, Collection<?> messages )
    {
        log( log, level, messages, null );
    }

    public static void log( Log log, int level, Collection<?> messages, CharSequence prefix )
    {
        if ( !isEnabled( log, level ) )
        {
            return;
        }

        for ( Object message : messages )
        {
            log( log, level, message, prefix );
        }
    }

    public static void log( Log log, int level, Object message )
    {
        log( log, level, message, null );
    }

    public static void log( Log log, int level, Object message, CharSequence prefix )
    {
        CharSequence chars;

        if ( message instanceof CharSequence )
        {
            chars = (CharSequence) message;
        }
        else
        {
            chars = String.valueOf( message );
        }

        log( log, level, chars, prefix );
    }

    public static void log( Log log, int level, CharSequence message )
    {
        if ( level == LEVEL_DEBUG )
        {
            log.debug( message );
        }
        else if ( level == LEVEL_INFO )
        {
            log.info( message );
        }
        else if ( level == LEVEL_WARN )
        {
            log.warn( message );
        }
        else if ( level == LEVEL_ERROR )
        {
            log.error( message );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown log level: " + level );
        }
    }

    public static void log( Log log, int level, CharSequence message, CharSequence prefix )
    {
        log( log, level, format( message, prefix ) );
    }

    public static boolean isEnabled( Log log, int level )
    {
        boolean enabled;

        if ( level == LEVEL_DEBUG )
        {
            enabled = log.isDebugEnabled();
        }
        else if ( level == LEVEL_INFO )
        {
            enabled = log.isInfoEnabled();
        }
        else if ( level == LEVEL_WARN )
        {
            enabled = log.isWarnEnabled();
        }
        else if ( level == LEVEL_ERROR )
        {
            enabled = log.isErrorEnabled();
        }
        else
        {
            throw new IllegalArgumentException( "Unknown log level: " + level );
        }

        return enabled;
    }

    // private methods --------------------------------------------------------

    private static CharSequence format( CharSequence message, CharSequence prefix )
    {
        if ( prefix == null || prefix.length() == 0 )
        {
            return message;
        }

        return new StringBuffer( prefix ).append( message ).toString();
    }
}