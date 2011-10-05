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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

/**
 * Provides utilities for working with Maven projects.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: MavenProjectUtils.java 12564 2010-09-15 16:09:11Z mark $
 */
public final class MavenProjectUtils
{
    // constructors -----------------------------------------------------------

    private MavenProjectUtils()
    {
        throw new AssertionError();
    }

    // public methods ---------------------------------------------------------

    /**
     * Gets a list of paths for the specified artifacts.
     *
     * @param project
     *            the project
     * @param artifacts
     *            the artifacts to obtain paths for
     * @return a list of <code>String</code> paths to the specified artifacts
     *             if an artifact cannot be found
     */
    public static List<String> getClasspathElements( MavenProject project, List<Artifact> artifacts ) throws Exception {
        // based on MavenProject.getCompileClasspathElements

        List<String> list = new ArrayList<String>( artifacts.size() );

        for ( Artifact artifact : artifacts )
        {
            if ( artifact.getArtifactHandler().isAddedToClasspath() )
            {
                // TODO: let the scope handler deal with this
                if ( Artifact.SCOPE_COMPILE.equals( artifact.getScope() )
                    || Artifact.SCOPE_RUNTIME.equals( artifact.getScope() ) )
                {
                    addArtifactPath( project, artifact, list );
                }
            }
        }

        return list;
    }

    /**
     * Gets whether the specified list of resources contains a resource with the specified directory.
     *
     * @param resources the list of resources to examine
     * @param directory the resource directory to look for
     * @return {@code true} if the list of resources contains a resource with the specified directory
     */
    public static boolean containsDirectory( List<Resource> resources, String directory )
    {
        for ( Resource resource : resources )
        {
            if ( directory.equals( resource.getDirectory() ) )
            {
                return true;
            }
        }

        return false;
    }

    // private methods --------------------------------------------------------

    // copied from MavenProject.addArtifactPath
    private static void addArtifactPath( MavenProject project, Artifact artifact, List<String> list ) throws Exception {
        String refId = getProjectReferenceId( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
        MavenProject refProject = (MavenProject) project.getProjectReferences().get( refId );

        boolean projectDirFound = false;
        if ( refProject != null )
        {
            if ( artifact.getType().equals( "test-jar" ) )
            {
                File testOutputDir = new File( refProject.getBuild().getTestOutputDirectory() );
                if ( testOutputDir.exists() )
                {
                    list.add( testOutputDir.getAbsolutePath() );
                    projectDirFound = true;
                }
            }
            else
            {
                list.add( refProject.getBuild().getOutputDirectory() );
                projectDirFound = true;
            }
        }
        if ( !projectDirFound )
        {
            File file = artifact.getFile();
            if ( file == null )
            {
                throw new Exception( artifact.toString() );
            }
            list.add( file.getPath() );
        }
    }

    // copied from MavenProject.getProjectReferenceId
    private static String getProjectReferenceId( String groupId, String artifactId, String version )
    {
        return groupId + ":" + artifactId + ":" + version;
    }
}