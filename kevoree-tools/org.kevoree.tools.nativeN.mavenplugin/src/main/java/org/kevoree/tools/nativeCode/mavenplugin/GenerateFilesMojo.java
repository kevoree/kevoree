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

    private final String sub_c = "-native";
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



    public  String generateJavaSources(AbstractCodeGenerator code) throws IOException
    {
        // Create Java Project Wrapper
        Model wrapper_java =     MavenHelper.createModel(project.getGroupId(), project.getArtifactId() +"-wrapper", project.getVersion(), MavenHelper.createParent(project), project);
        wrapper_java.setName(project.getName() + " :: Wrapper Java");

        getLog().info("Create Wrapper Pom");

        MavenHelper.createPom("poms/pom.xml.component", wrapper_java.getPomFile().getPath(),wrapper_java, project,"");

        getLog().info("Generating files");
        String root_build =   inputCFile.getPath();
        /// GENERATE JAVA FILES
        File file_java = new File(root_build+"src/main/java/org/kevoree/library/nativeN/"+code.getComponentType().getName());

        if(file_java.mkdirs())
        {
            String bridge = new String(FileManager.load(GenerateFilesMojo.class.getClassLoader().getResourceAsStream("template_java/Bridge.java.template")));
            bridge = bridge.replace("$PACKAGE$","package org.kevoree.library.nativeN."+code.getComponentType().getName()+";");
            bridge = bridge.replace("$HEADER_PORTS$",code.getHeaderPorts());
            bridge = bridge.replace("$PORTS$", code.getPorts());
            bridge = bridge.replace("$CLASS$",code.getComponentType().getName());
            bridge = bridge.replace("$artifactId$", project.getArtifactId() +"-wrapper");
            bridge = bridge.replace("$groupId$",project.getGroupId());
            bridge = bridge.replace("$version$", project.getVersion());
            bridge = bridge.replace("$DICO$",code.getBody());
            bridge = bridge.replace("$DICO$",code.getBody());
            MavenHelper.writeFile(file_java.getPath() + "/" + code.getComponentType().getName() + ".java", bridge, false,false);
        }  else
        {
            getLog().error("Generating component java");
        }
        // add module
        //   project.getModel().addModule("./" + wrapper_java.getArtifactId());
        return wrapper_java.getArtifactId();
    }


    public String generateCSources(AbstractCodeGenerator code) throws IOException {

        String componentName = code.getComponentType().getName();

        // Create NativeCode Pom Root
        Model component_c=     MavenHelper.createModel(project.getGroupId(), project.getArtifactId() + sub_c, project.getVersion(), MavenHelper.createParent(project), project);
        component_c.setName(project.getName()+" :: NativeCode ");

        // Create sub NativeCode {arm,osx,nix32,nix64}
        MavenHelper.createPom("poms/pom.xml.c.profil", component_c.getPomFile().getPath(),component_c, project,"");
        MavenHelper.createPom("poms/pom.xml.c.nix32",component_c.getPomFile().getPath().replace("pom.xml", "nix32/pom.xml"), component_c, project,sub_c);
        MavenHelper.createPom("poms/pom.xml.c.nix64", component_c.getPomFile().getPath().replace("pom.xml","nix64/pom.xml"),component_c, project,sub_c);
        MavenHelper.createPom("poms/pom.xml.c.osx",component_c.getPomFile().getPath().replace("pom.xml", "osx/pom.xml"), component_c,project,sub_c);
        MavenHelper.createPom("poms/pom.xml.c.arm", component_c.getPomFile().getPath().replace("pom.xml", "arm/pom.xml"),component_c, project,sub_c);

        /// GENERATE C FILES
        String path_c = component_c.getPomFile().getAbsolutePath().replace("pom.xml","")+"src/main/c/";

        File file_c = new File(path_c);
        if(file_c.mkdirs())
        {

            getLog().info("-> "+componentName+".c");
            MavenHelper.writeFile(file_c.getPath() + "/" + componentName + ".c", code.getHeaderPorts().replace("$NAME$", componentName),false,true);
            getLog().info("-> " + componentName + ".h");
            MavenHelper.writeFile(file_c.getPath() + "/" + componentName + ".h", code.getBody(),false,true);
            // lib
            File file = new File(path_c+"/thirdparty");
            if(file.mkdir())
            {
                getLog().info("-> Thirdparty");
                for(String n : files_thirdparty)
                {
                    FileManager.copyFileFromStream(GenerateFilesMojo.class.getClassLoader().getResourceAsStream(n), file.getPath(), n,true);
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
        // add module
        //   project.getModel().addModule("./" + component_c.getArtifactId());
        // project.getModel().setBuild(null);
        //project.getModel().setPackaging("pom");

        return component_c.getArtifactId();
    }



    /**
     * Generate interfaces
     * @param componentName
     * @param path_to_kevScript_file
     * @param path_out
     * @throws Exception
     */
    public  void generateSources(String componentName, String path_to_kevScript_file, String path_out) throws Exception {

        getLog().info("Reading KevScript");

        // getting model from KevScript where CreatingType exist
        ContainerRoot model =  KevScriptLoader.getModel(path_to_kevScript_file);
        //Set model to generators
        AbstractCodeGenerator codeGeneratorJava = new CodeGeneratorJava(model);
        AbstractCodeGenerator codeGeneratorC = new CodeGeneratorC(model);
        getLog().info("Generate sources");
        // Generate JAVA CODE according with the model
        codeGeneratorJava.execute();
        // Generate C CODE according with the model
        codeGeneratorC.execute();

        String ArtifactIdJava = generateJavaSources(codeGeneratorJava);
        String ArtifactIdC = generateCSources(codeGeneratorC);

        String path_pom_root =project.getModel().getPomFile().getPath();

        String pom_root = new String(FileManager.load(path_pom_root));

        if(!pom_root.contains("modules"))
        {
            String modules = "    <modules>\n" +
                    "        <module>"+ArtifactIdJava+"</module>\n" +
                    "        <module>"+ArtifactIdC+"</module>\n" +
                    "    </modules>\n" +
                    "</project>";
            MavenHelper.writeFile(path_pom_root,pom_root.replace("</project>",modules),false,false);
        }

    }

}
