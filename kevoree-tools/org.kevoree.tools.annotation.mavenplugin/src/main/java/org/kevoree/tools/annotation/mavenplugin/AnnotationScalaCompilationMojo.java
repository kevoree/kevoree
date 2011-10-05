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
package org.kevoree.tools.annotation.mavenplugin;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;


/**
 * @author ffouquet
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal compile
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class AnnotationScalaCompilationMojo extends AbstractMojo {

        /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/kevoree"
     */
    private File sourceOutputDirectory;

    /**
     *
     * @parameter default-value="${project.build.directory}/classes"
     */
    private File outputClasses;

        /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private ScalaCompiler ScalaCompiler = new ScalaCompiler();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Integer result = ScalaCompiler.compile(sourceOutputDirectory.getPath(),outputClasses.getPath(),project.getCompileClasspathElements());
            if(result != 0){
                getLog().error("Embedded Scala compilation error !");
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }








}
