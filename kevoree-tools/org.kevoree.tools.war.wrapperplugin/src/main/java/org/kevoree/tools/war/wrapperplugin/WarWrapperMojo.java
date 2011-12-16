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
package org.kevoree.tools.war.wrapperplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;

/**
 * @author ffouquet
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class WarWrapperMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.build.directory}"
     */
    private File tempWar;

    /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/kevoree"
     */
    private File sourceOutputDirectory;

    /**
     * @parameter default-value="${project.build.directory}/warcontent"
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

    /**
     * Origin war file
     *
     * @parameter
     * @required
     */
    private String war;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            URL url = new URL(war);
            String targetFile = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
            File tempWarFile = new File(tempWar.getAbsolutePath()+File.separator+targetFile);
            if(!tempWarFile.exists()){
                getLog().info("Dowloading = "+war +" -> "+tempWarFile.getAbsolutePath());
                UrlHelper.getFile(war,tempWarFile);
            }
            ZipHelper.unzipToTempDir(tempWarFile,outputClasses);

            project.getProperties().put("warlibs","ton.jar");


            project.getB


        } catch (Exception e) {
            getLog().error(e);
        }
    }
}