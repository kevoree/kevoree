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
package org.kevoree.platform.mavenrunner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.platform.standalone.App;
import org.kevoree.platform.standalone.KevoreeBootStrap;
import org.kevoree.tools.aether.framework.AetherUtil;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import java.io.File;
import java.io.FileInputStream;

/**
 * User: Francois Fouquet - fouquet.f@gmail.com
 * Date: 15/03/12
 * Time: 12:58
 *
 * @author Francois Fouquet
 * @version 1.0
 * @goal run
 * @phase install
 * @requiresDependencyResolution compile+runtime
 */
public class KevRunnerMavenMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project.basedir}/src/main/kevs/main.kevs"
	 */
	private File model;

	/**
	 * @parameter default-value="node0"
	 */
	private String targetNode;

	/**
	 * The maven project.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

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


	public void execute () throws MojoExecutionException {

		try {
			KevoreeBootStrap.byPassAetherBootstrap = true;
			AetherUtil.instance$.setRepositorySystemSession(repoSession);
			AetherUtil.instance$.setRepositorySystem(repoSystem);
			ContainerRoot modelRoot = null;
			if (model.getName().endsWith(".kev")) {
				FileInputStream ins = new FileInputStream(model);
				modelRoot = KevoreeXmiHelper.instance$.loadStream(ins);
				ins.close();
			} else if (model.getName().endsWith(".kevs")) {
				modelRoot = KevScriptHelper.generate(model, project);
			} else {
				throw new Exception("Bad input file, must be .kev or .kevs");
			}

			File tFile = new File(project.getBuild().getOutputDirectory(), "runner.kev");
			KevoreeXmiHelper.instance$.save(tFile.getAbsolutePath(), modelRoot);

//			System.setProperties(project.getProperties());
			for (Object key : project.getProperties().keySet()) {
				System.setProperty(key.toString(), project.getProperties().get(key).toString());
			}
			System.setProperty("node.bootstrap", tFile.getAbsolutePath());
			if (System.getProperty("node.name") == null) {
				System.setProperty("node.name", targetNode);
			}

			App.main(new String[0]);

			Thread.currentThread().join();

		} catch (Throwable e) {
			getLog().error(e);
		}


	}


}
