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
package org.kevoree.platform.mavenrunner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.platform.standalone.KevoreeBootStrap;
import org.kevoree.tools.modelsync.ModelSyncBean;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 10:26
 */


/**
 * User: Francois Fouquet - fouquet.f@gmail.com
 * Date: 15/03/12
 * Time: 12:58
 *
 * @author Francois Fouquet
 * @version 1.0
 * @goal push
 * @phase deploy
 * @requiresDependencyResolution compile+runtime
 */
public class KevDeployMavenMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.basedir}/src/main/kevs/main.kevs"
     */
    private File model;

    /**
     * @parameter default-value="node0"
     */
    private String targetNode;

    /**
     * @parameter default-value="sync"
     */
    private String viaGroup;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;


    private ModelSyncBean bean = new ModelSyncBean();


    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            //Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            //root.setLevel(Level.ALL);
            KevoreeBootStrap.byPassAetherBootstrap = true;
            ContainerRoot modelLoad = null;
            if (model.getName().endsWith(".kev")) {
                FileInputStream ins = new FileInputStream(model);
                modelLoad = KevoreeXmiHelper.instance$.loadStream(ins);
                ins.close();
            } else if (model.getName().endsWith(".kevs")) {
                modelLoad = KevScriptHelper.generate(model, project);
            } else {
                throw new Exception("Bad input file, must be .kev or .kevs");
            }

            bean.pushTo(modelLoad, targetNode, viaGroup);
            getLog().info("Model pushed on " + targetNode + " via " + viaGroup);
        } catch (Exception e) {
            getLog().error("Error while loading model ", e);
        }
    }

}
