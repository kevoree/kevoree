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
package org.kevoree.tools.org.kevoree.tools.osgi.sysgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @author ffouquet
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class SysGenPlugin extends AbstractMojo {

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
     * Target file
     *
     * @parameter
     */
    private File targetFileName;
    /**
     * Target file
     *
     * @parameter
     */
    private Boolean debug = false;
    /**
     * Filters
     *
     * @parameter
     */
    private String filters;
    /**
     * @parameter default-value="${project.build.directory}/generated-sources/sysgen"
     */
    private File sourceOutputDirectory;

    public String cleanVersion(String previousVersion) {
        String finalVersion = "";
        String ppreviousVersion = previousVersion;
        if (ppreviousVersion.contains("-")) {
            ppreviousVersion = ppreviousVersion.substring(0, ppreviousVersion.indexOf("-"));
        }
        String[] currentVersionTab = ppreviousVersion.split("\\.");
        for (int i = 0; i < currentVersionTab.length; i++) {

            String temp = currentVersionTab[i];
            String finalTemp = "";
            char[] chars = temp.toCharArray();
            for (int j = 0; j < chars.length; j++) {
                if (Character.isDigit(chars[j])) {
                    finalTemp = finalTemp + chars[j];
                }
            }

            if (!finalTemp.equals("")) {
                if (i != 0) {
                    finalVersion = finalVersion +".";
                }
                finalVersion = finalVersion + finalTemp;
            }
        }
        return finalVersion;
    }


    @Override
    public void execute() throws MojoExecutionException {


        Map<String, String> packages = new HashMap<String, String>();
        //   Set<String> packagesVersion = new HashSet<String>();

        List<String> filters2 = Arrays.asList(filters.split(","));

        Iterator it2 = project.getCompileArtifacts().iterator();

//        System.out.println(project.getDependencyArtifacts().size());
        //    System.out.println(project.getCompileArtifacts().size());
        //   System.out.println(project.getAttachedArtifacts().size());

        while (it2.hasNext()) {
            Artifact d = (Artifact) it2.next();
            String artefactPath = local.getBasedir() + "/" + local.pathOf(d).toString();
            Set<String> newPackages = PackageUtils.getFilteredPackageNames(artefactPath, filters2, debug);
            for (String packageName : newPackages) {
                String version = cleanVersion(d.getVersion());

                if (!packages.keySet().contains(packageName)) {
                    packages.put(packageName, version);
                    //  packagesVersion.add(packageName+ ";version=" + version);
                } else {
                    getLog().warn("Doublon " + packageName + d.getVersion());

                    String[] oldversions = packages.get(packageName).split("\\.");
                    String[] newVersions = version.split("\\.");
                    Boolean sup = false;

                    int maxI = Math.max(oldversions.length, newVersions.length);
                    for (int i = 0; i < maxI; i++) {
                        if (i < oldversions.length && i < newVersions.length) {
                            sup = sup || (Integer.parseInt(newVersions[i]) > Integer.parseInt(oldversions[i]));
                        }
                    }

                    if (sup) {
                        packages.put(packageName, version);
                    }
                }
            }
        }

        //FLAT VERSION
        Set<String> packagesVersion = new HashSet<String>();
        Set<String> keys = packages.keySet();
        for (String key : keys) {
            packagesVersion.add(key + ";version=" + packages.get(key));
        }

        String content = SysStaticClassGenerator.generate(packagesVersion);
        try {
            generateFile(new File(sourceOutputDirectory.getAbsolutePath() + File.separatorChar + "generated"), "SysPackageConstants.java", content);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SysGenPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SysGenPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.project.addCompileSourceRoot(sourceOutputDirectory.getAbsolutePath());
        this.getLog().info("Source directory: " + this.sourceOutputDirectory + " added.");
    }

    public void generateFile(File directory, String fileName, String content) throws FileNotFoundException, IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        FileOutputStream output = new java.io.FileOutputStream(directory.getAbsolutePath() + File.separatorChar + fileName);
        PrintWriter writer = new PrintWriter(output);
        writer.println(content);
        writer.flush();
        writer.close();
        output.close();
    }
}
