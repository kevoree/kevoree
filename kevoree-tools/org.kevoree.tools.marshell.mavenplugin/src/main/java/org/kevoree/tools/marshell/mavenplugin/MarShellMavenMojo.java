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
package org.kevoree.tools.marshell.mavenplugin;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.merger.KevoreeMergerComponent;
import org.kevoree.tools.aether.framework.AetherUtil;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;

import java.io.File;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/03/12
 * Time: 12:58
 *
 * @author Erwan Daubert
 * @version 1.0
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class MarShellMavenMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project.basedir}/src/main/marshell"
	 */
	private File sourceMarShellDirectory;

	/**
	 * The directory root under which processor-generated source files will be placed; files are placed in
	 * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
	 *
	 * @parameter default-value="${project.build.directory}/generated-sources/kevoree/KEV-INF"
	 */
	private File sourceOutputDirectory;
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

	private KevoreeMergerComponent mergerComponent;

	public void execute () throws MojoExecutionException {

		AetherUtil.setRepositorySystemSession(repoSession);
		AetherUtil.setRepositorySystem(repoSystem);

		mergerComponent = new KevoreeMergerComponent();

		ContainerRoot model = executeOnDirectory(sourceMarShellDirectory);
		if (!sourceOutputDirectory.exists() && !sourceOutputDirectory.mkdirs()) {
			throw new MojoExecutionException("Unable to build target packages " + sourceOutputDirectory.getAbsolutePath());
		}
		KevoreeXmiHelper.save(sourceOutputDirectory.getAbsolutePath() + File.separator + "lib.kev", model);

		Resource resource = new Resource();
		resource.setTargetPath("KEV-INF");
		resource.setDirectory(sourceOutputDirectory.getAbsolutePath());

		project.addResource(resource);
	}

	private ContainerRoot executeOnDirectory (File dir) throws MojoExecutionException {
		ContainerRoot mergedModel = KevoreeFactory.createContainerRoot();
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				ContainerRoot model = executeOnDirectory(f);
				mergedModel = mergerComponent.merge(mergedModel, model);
			} else {
//				try {
				ContainerRoot model = ModelGenerator.generate(f.getAbsolutePath());
				mergedModel = mergerComponent.merge(mergedModel, model);
				/*} catch (Exception e) {
					getLog().error("Unable to parse the source file", e);
				}*/
			}
		}
		return mergedModel;
	}
}
