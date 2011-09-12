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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * Provides methods of invoking the Annotation Processing Tool (apt) compiler.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: AptUtils.java 11286 2009-11-21 16:02:42Z bentmann $
 */
public final class AptUtils
{
    // constants --------------------------------------------------------------

    /**
     * The line separator for the current platform.
     */
    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    // fields -----------------------------------------------------------------

    // constructors -----------------------------------------------------------

    private AptUtils()
    {
        throw new AssertionError();
    }

    // public methods ---------------------------------------------------------

    public static boolean invoke( Log log, List<String> args ) throws MojoExecutionException
    {
        // get apt method

        Class<?> apt = getAptClass();

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter( stringWriter, true );
        String[] argsArray = args.toArray( new String[args.size()] );

        Method method;
        Object[] methodArgs;

        try
        {
            method = apt.getMethod( "process", new Class[] { PrintWriter.class, String[].class } );

            methodArgs = new Object[] { writer, argsArray };
        }
        catch ( NoSuchMethodException exception )
        {
            try
            {
                method = apt.getMethod( "compile", new Class[] { String[].class, PrintWriter.class } );

                methodArgs = new Object[] { argsArray, writer };
            }
            catch ( NoSuchMethodException exception2 )
            {
                throw new MojoExecutionException( "Error while executing the apt compiler", exception2 );
            }
        }

        // invoke apt

        log.debug( "Invoking apt with arguments:" );
        LogUtils.log( log, LogUtils.LEVEL_DEBUG, args, "  " );

        int result;

        try
        {
            result = ( (Integer) method.invoke( null, methodArgs ) ).intValue();
        }
        catch ( IllegalAccessException exception )
        {
            throw new MojoExecutionException( "Error while executing the apt compiler", exception );
        }
        catch ( InvocationTargetException exception )
        {
            throw new MojoExecutionException( "Error while executing the apt compiler", exception );
        }

        // log output

        LogUtils.log( log, LogUtils.LEVEL_WARN, new StringReader( stringWriter.toString() ) );

        // log result

        log.debug( "Apt returned " + result );

        return ( result == 0 );
    }

    public static boolean invokeForked( Log log, File workingDirectory, String executable, String meminitial,
                                        String maxmemory, List<String> args ) throws MojoExecutionException
    {
        // create command

        Commandline cli = new Commandline();

        cli.setWorkingDirectory( workingDirectory.getAbsolutePath() );
        log.debug( "Using working directory " + cli.getWorkingDirectory() );

        cli.setExecutable( executable );

        if ( StringUtils.isNotEmpty( meminitial ) )
        {
            cli.createArg().setValue( "-J-Xms" + meminitial );
        }

        if ( StringUtils.isNotEmpty( maxmemory ) )
        {
            cli.createArg().setValue( "-J-Xmx" + maxmemory );
        }

        // create arguments file

        File argsFile;

        try
        {
            argsFile = createArgsFile( args );

            String argsPath = argsFile.getCanonicalPath().replace( File.separatorChar, '/' );
            cli.createArg().setValue( "@" + argsPath );
        }
        catch ( IOException exception )
        {
            throw new MojoExecutionException( "Error creating arguments file", exception );
        }

        log.debug( "Using argument file:" );
        LogUtils.log( log, LogUtils.LEVEL_DEBUG, argsFile, "  " );

        // invoke apt

        if ( log.isDebugEnabled() )
        {
            log.debug( "Invoking apt with command " + cli );
        }

        StringStreamConsumer out = new StringStreamConsumer();
        StringStreamConsumer err = new StringStreamConsumer();

        int result;

        try
        {
            result = CommandLineUtils.executeCommandLine( cli, out, err );
        }
        catch ( CommandLineException exception )
        {
            throw new MojoExecutionException( "Error while executing the apt compiler", exception );
        }

        // log output

        LogUtils.log( log, LogUtils.LEVEL_INFO, new StringReader( out.getOutput() ) );
        LogUtils.log( log, LogUtils.LEVEL_WARN, new StringReader( err.getOutput() ) );

        // log result

        log.debug( "Apt returned " + result );

        return ( result == 0 );
    }

    // private methods --------------------------------------------------------

    private static Class<?> getAptClass() throws MojoExecutionException
    {
        try
        {
            //return Class.forName( "com.sun.tools.apt.Main");
            return Class.forName( "com.sun.tools.apt.Main", true, AptClassLoader.INSTANCE );
        }
        catch ( ClassNotFoundException exception )
        {
            throw new MojoExecutionException( "Unable to locate the apt compiler in:" + LINE_SEPARATOR
                + "  " + AptClassLoader.getToolsJar() + LINE_SEPARATOR
                + "Please ensure you are using JDK 1.5 or above and" + LINE_SEPARATOR
                + "not a JRE (the com.sun.tools.apt.Main class is required)." + LINE_SEPARATOR
                + "In most cases you can change the location of your Java" + LINE_SEPARATOR
                + "installation by setting the JAVA_HOME environment variable." );
        }
    }

    private static File createArgsFile( List<String> args ) throws IOException
    {
        File file = File.createTempFile( AptUtils.class.getName(), ".argfile" );
        file.deleteOnExit();

        PrintWriter writer = null;

        try
        {
            writer = new PrintWriter( new FileWriter( file ) );

            for ( String arg : args )
            {
                arg = arg.replace( File.separatorChar, '/' );

                if ( arg.contains( " " ) )
                {
                    arg = "\"" + arg + "\"";
                }

                writer.println( arg );
            }
        }
        finally
        {
            IOUtil.close( writer );
        }

        return file;
    }
}