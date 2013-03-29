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
package org.kevoree.tools.annotation.mavenplugin;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jetbrains.jet.cli.common.ExitCode;
import org.jetbrains.jet.cli.jvm.K2JVMCompiler;
import org.jetbrains.jet.cli.jvm.K2JVMCompilerArguments;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;


/**
 * @author ffouquet
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal compile
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class AnnotationKotCompilationMojo extends AbstractMojo {

    /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/kevoree"
     */
    private File sourceOutputDirectory;

    /**
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

    K2JVMCompiler compiler = new K2JVMCompiler();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            K2JVMCompilerArguments args = new K2JVMCompilerArguments();
            StringBuffer cpath = new StringBuffer();
            boolean firstBUF = true;
            for (String path : project.getCompileClasspathElements()) {
                if (!firstBUF) ;
                cpath.append(":");
                cpath.append(path);
                firstBUF = false;
            }
            args.setClasspath(cpath.toString());
            args.setSourceDirs(Collections.singletonList(sourceOutputDirectory.getPath()));
            args.setOutputDir(outputClasses.getPath());
            args.noJdkAnnotations = true;
            args.noStdlib = true;
            ExitCode e = compiler.exec(System.err, args);
            if (e.ordinal() != 0) {
                throw new MojoExecutionException("Embedded Kotlin compilation error !");
            }
        } catch (MojoExecutionException e) {
            getLog().error(e);
            throw e;
        } catch (Exception e) {
            getLog().error(e);
        }
    }


}
