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
package org.kevoree.tools.nativeCode.mavenplugin.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.kevoree.KevoreeFactory;
import org.kevoree.tools.nativeCode.mavenplugin.GenerateFilesMojo;
import org.kevoree.tools.nativeN.utils.FileManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 09/10/12
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class MavenHelper {


    /* Check file header */
    public static  List<File> scanForKevScript(File modelDir) {
        List<File> models = new ArrayList();
        for (File f : modelDir.listFiles()) {
            if(f.isDirectory()){
                return scanForKevScript(f);
            } else
            {
                if (f.getName().endsWith(".kevs"))
                {
                    String file = new String(FileManager.load(f.getAbsolutePath()));
                    if(file.contains("createComponentType")){
                        models.add(f);
                    }
                }
            }
        }
        return models;
    }


    public static  void createPom(String path_template,Model model,MavenProject project,String path,String name ) throws IOException {

        String pom_component = new String(FileManager.load(GenerateFilesMojo.class.getClassLoader().getResourceAsStream(path_template)));

        pom_component= pom_component.replace("$groupId$", model.getGroupId());
        pom_component= pom_component.replace("$artifactId$",model.getArtifactId());
        pom_component= pom_component.replace("$version$", model.getArtifactId());
        pom_component= pom_component.replace("$NAME$",model.getName());
        pom_component = pom_component.replace("$VERSION_K$", KevoreeFactory.getVersion());

        pom_component = pom_component.replace("$artifactId_parent$",project.getArtifactId()+name);
        pom_component = pom_component.replace("$version_parent$",project.getVersion());

        MavenHelper.writeFile(path,pom_component,false);
    }
    public static void writeModel(Model model)   throws IOException
    {
        FileWriter writer = null;
        try
        {
            model.getPomFile().getParentFile().mkdirs();
            writer = new FileWriter( model.getPomFile() );
            new MavenXpp3Writer().write( writer, model );

            writer.flush();
        }
        finally
        {
            IOUtil.close(writer);
        }
    }


    public static Parent createParent(MavenProject project)
    {
        return createParent( project.getGroupId(), project.getArtifactId(), project.getVersion() );
    }

    private static Parent createParent( Model model )
    {
        return createParent( model.getGroupId(), model.getArtifactId(), model.getVersion() );
    }

    private static Parent createParent( String groupId, String artifactId, String version )
    {
        Parent plugin = new Parent();
        plugin.setGroupId( groupId );
        plugin.setArtifactId( artifactId );
        plugin.setVersion( version );
        return plugin;
    }

    public static Model createModel(String groupId, String artifactId, String version, Parent parent, MavenProject parentProject) throws IOException {
        Model model = new Model();
        model.setArtifactId( artifactId );
        model.setGroupId( groupId );
        model.setVersion( version );
        model.setModelVersion("4.0.0");
        model.setParent(parent);
        model.setName("");

        File parentBase = parentProject.getBasedir();

        File  pomFile = new File( parentBase, artifactId+"/pom.xml");

        model.setPomFile(pomFile);

        return model;
    }

    public static void writeFile(String path,String data,Boolean append) throws IOException {
        File createpath = new File(path.substring(0,path.lastIndexOf("/")));
        createpath.mkdirs();

        FileWriter fileWriter = new FileWriter(path,append);
        BufferedWriter out_j = new BufferedWriter(fileWriter);
        out_j.write(data);
        out_j.close();
    }
}
