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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.tools.nativeCode.mavenplugin;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.kevoree.*;
import org.kevoree.tools.nativeCode.mavenplugin.utils.MavenHelper;
import org.kevoree.tools.nativeN.utils.KevScriptLoader;
import org.kevoree.tools.nativeN.generator.AbstractCodeGenerator;
import org.kevoree.tools.nativeN.generator.CodeGeneratorC;
import org.kevoree.tools.nativeN.generator.CodeGeneratorJava;
import org.kevoree.tools.nativeN.utils.FileManager;

import java.io.*;
import java.util.List;

/**
 * @author jedartois
 * @author <a href="mailto:jedartois@gmail.com">Jean-Emile DARTOIS</a>
 * @version $Id$
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class GenerateFilesMojo extends AbstractMojo {

    /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/kevoree"
     */
    private File sourceOutputDirectory;

    /**
     *
     * @parameter default-value="${basedir}/src/main"
     */
    private File inputCFile;


    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */

    protected MavenProject project;

    private final String sub_c = "_native";
    private final String sub_java = "_bridge";
    final String[] files_thirdparty = { "component.h","kqueue.h","events_common.h","events_fifo.h","HashMap.h","settings.h"};

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(inputCFile == null || !(inputCFile.exists())){
            getLog().warn("InputDir null => ");
        } else {
            List<File> componentFiles = MavenHelper.scanForKevScript(inputCFile);
            System.out.println(inputCFile + " size = " + componentFiles.size());
            for(File f : componentFiles){
                getLog().info("File found =>"+f.getAbsolutePath());
                try
                {
                    generateSources(f.getName().replace(".kevs", ""), f.getPath(), f.getPath().replace(f.getName(), ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean createDirectory(String path)
    {

   return true;
    }

    /**
     * Generate interfaces
     * @param componentName
     * @param path_to_kevScript_file
     * @param path_out
     * @throws Exception
     */
    public  void generateSources(String componentName, String path_to_kevScript_file, String path_out) throws Exception {

        ContainerRoot model =  KevScriptLoader.getModel(path_to_kevScript_file);

        // Gen JAVA CODE
        AbstractCodeGenerator codeGeneratorJava = new CodeGeneratorJava(model);
        codeGeneratorJava.execute();

       // Gen C CODE
        AbstractCodeGenerator codeGeneratorC = new CodeGeneratorC(model);
        codeGeneratorC.execute();


        getLog().info("Reading poms");

        // Reading poms
        Model component_java =     MavenHelper.createModel(project.getGroupId(), project.getArtifactId() + sub_java, project.getVersion(), MavenHelper.createParent(project), project);
        component_java.setName(project.getName()+" :: Bridge Java");
        Model component_c=     MavenHelper.createModel(project.getGroupId(), project.getArtifactId() + sub_c, project.getVersion(), MavenHelper.createParent(project), project);
        component_c.setName(project.getName()+" :: NativeCode ");

        MavenHelper.createPom("poms/pom.xml.component", component_java, project,component_java.getPomFile().getPath(),"");
        MavenHelper.createPom("poms/pom.xml.c.profil", component_c, project,component_c.getPomFile().getPath(),"");
        MavenHelper.createPom("poms/pom.xml.c.nix32", component_c, project,component_c.getPomFile().getPath().replace("pom.xml","nix32/pom.xml"),sub_c);
        MavenHelper.createPom("poms/pom.xml.c.nix64", component_c, project,component_c.getPomFile().getPath().replace("pom.xml","nix64/pom.xml"),sub_c);
        MavenHelper.createPom("poms/pom.xml.c.osx", component_c, project,component_c.getPomFile().getPath().replace("pom.xml", "osx/pom.xml"),sub_c);
        MavenHelper.createPom("poms/pom.xml.c.arm", component_c, project,component_c.getPomFile().getPath().replace("pom.xml", "arm/pom.xml"),sub_c);


        getLog().info("Generating files");
        /// GENERATE JAVA FILES
        File file_java = new File(component_java.getPomFile().getAbsolutePath().replace("pom.xml","")+"src/main/java/org/kevoree/library/nativeN/"+componentName);

        if(file_java.mkdirs())
        {
            String bridge = new String(FileManager.load(GenerateFilesMojo.class.getClassLoader().getResourceAsStream("template_java/Bridge.java.template")));
            bridge = bridge.replace("$PACKAGE$","package org.kevoree.library.nativeN."+componentName+";");
            bridge = bridge.replace("$HEADER_PORTS$",codeGeneratorJava.getHeaderPorts());
            bridge = bridge.replace("$PORTS$",codeGeneratorJava.getPorts());
            bridge = bridge.replace("$CLASS$",componentName);
            bridge = bridge.replace("$artifactId$",component_c.getArtifactId());
            bridge = bridge.replace("$groupId$",component_c.getGroupId());
            bridge = bridge.replace("$version$",project.getVersion());
            bridge = bridge.replace("$DICO$",codeGeneratorJava.getBody());
            bridge = bridge.replace("$DICO$",codeGeneratorJava.getBody());

            MavenHelper.writeFile(file_java.getPath() + "/" + componentName + ".java", bridge, false);
        }  else
        {
            getLog().error("Generating component java");
        }



        /// GENERATE C FILES
        String path_c = component_c.getPomFile().getAbsolutePath().replace("pom.xml","")+"src/main/c/";

        File file_c = new File(path_c);
        if(file_c.mkdirs())
        {

            getLog().info("-> "+componentName+".c");
            MavenHelper.writeFile(file_c.getPath() + "/" + componentName + ".c", codeGeneratorC.getHeaderPorts().replace("$NAME$", componentName), false);

            getLog().info("-> " + componentName + ".h");

            MavenHelper.writeFile(file_c.getPath() + "/" + componentName + ".h", codeGeneratorC.getBody(), false);

            // lib
            File file = new File(path_c+"/thirdparty");
            if(file.mkdir())
            {
                getLog().info("-> Thirdparty");
                for(String n : files_thirdparty)
                {
                    FileManager.copyFileFromStream(GenerateFilesMojo.class.getClassLoader().getResourceAsStream(n), file.getPath(), n);
                }


            }  else
            {
                getLog().error("Creating thirdparty directory");
            }

        }
        else
        {
            getLog().error("Creating sources directory");
        }


        // add modules
        project.getModel().addModule("./" + component_java.getArtifactId());
        project.getModel().addModule("./" + component_c.getArtifactId());
        project.getModel().setBuild(null);
        project.getModel().setPackaging("pom");
        //MavenHelper.writeModel(project.getModel());

        String path_pom_root =project.getModel().getPomFile().getPath();

        String pom_root = new String(FileManager.load(path_pom_root));

       if(!pom_root.contains("modules"))
       {
           String modules = "    <modules>\n" +
                   "        <module>"+component_c.getArtifactId()+"</module>\n" +
                   "        <module>"+component_java.getArtifactId()+"</module>\n" +
                   "    </modules>\n" +
                   "</project>";
           MavenHelper.writeFile(path_pom_root,pom_root.replace("</project>",modules),false);
       }

    }

}
