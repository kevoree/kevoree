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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.tools.merger.mavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.merger.KevoreeMergerComponent;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ffouque
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class MergerMojo extends AbstractMojo {

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected java.util.List remoteRepos;
    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected org.apache.maven.artifact.repository.ArtifactRepository local;
    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;
    /**
     * Ecore file
     *
     * @parameter
     * @required
     */
    private File model;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (model.isDirectory()) {
            for (File f : scanForArt2(model)) {
                mergeFile(f);
            }

        } else {
            mergeFile(model);
        }
    }

    private List<File> scanForArt2(File modelDir) {
        List<File> models = new ArrayList();
        for (File f : modelDir.listFiles()) {
            if (f.getName().endsWith(".kev")) {
                models.add(f);
            }
        }
        return models;
    }

    private void mergeFile(File modelInput) {

        this.getLog().info("Process Kevoree Model => " + modelInput);

        //CREATE NEW MERGER INSTANCE
        KevoreeMergerComponent merger = new KevoreeMergerComponent();

        //LOAD PREVIOUS MODEL OR CREATE ONE
        ContainerRoot root = null;
       // if (modelInput.isFile()) {
          //  root = KevoreeXmiHelper.load(modelInput.getAbsolutePath());
       // } else {
       //     this.getLog().warn("Model File Empty, creating one !");
            root = KevoreeFactory.eINSTANCE().createContainerRoot();
        //}

        //MERGE TWO BY TWO
        Iterator it2 = project.getDependencyArtifacts().iterator();
        while (it2.hasNext()) {
            Artifact d = (Artifact) it2.next();
            String artefactPath = local.getBasedir() + "/" + local.pathOf(d).toString();

            JarFile jar;
            try {
                jar = new JarFile(artefactPath);
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                if (entry != null) {
                    String path = convertStreamToFile(jar.getInputStream(entry));
                    //Load
                    ContainerRoot nroot = KevoreeXmiHelper.load(path);
                    //Merge
                    this.getLog().info("Kevoree Merge from => " + artefactPath);
                    merger.merge(root, nroot);

                    //CREATE TEMP FILE FROM ACTUAL MODEL
                    /*
                    String baseDir = ("file:\\"+modelInput.getAbsolutePath()).substring(5).replace('\\', '/');
                    String result = "file:" + baseDir;
                    if (baseDir.indexOf(":") > 0) {
                    	result = "file:///" + baseDir;
                    }*/
                    this.getLog().info("ModelOutput => " + modelInput.getAbsoluteFile().toURI());

                    //    Art2XmiHelper.save(java.net.URLStreamHandler.toExternalForm(modelInput.getAbsoluteFile().g), root);

                    KevoreeXmiHelper.save(modelInput.getAbsolutePath(), root);
                }
            } catch (IOException ex) {
                this.getLog().error(ex);
            }
        }

    }

    private String convertStreamToFile(InputStream inputStream) throws IOException {
        Random rand = new Random();
        File temp = File.createTempFile("art2loaderLib" + rand.nextInt(), ".xmi");
        // Delete temp file when program exits.
        temp.deleteOnExit();
        OutputStream out = new FileOutputStream(temp);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        inputStream.close();
        out.flush();
        out.close();

        return  temp.getAbsolutePath().toString();
    }
}
