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
import org.kevoree.framework.KevoreeXmiHelper$;
import org.kevoree.tools.modelsync.ModelSyncBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
     * @parameter
     */
    private File model;

    /**
     * @parameter
     */
    private String targetNode;

    /**
     * @parameter
     */
    private String viaGroup;


    private ModelSyncBean bean = new ModelSyncBean();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            FileInputStream ins = new FileInputStream(model);
            bean.pushTo(org.kevoree.framework.KevoreeXmiHelper.loadStream(ins), targetNode, viaGroup);
        } catch (Exception e) {
            getLog().error("Error while loading model");
        }
    }

}
