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
package org.kevoree.tools.war.wrapperplugin

import java.io.{FileWriter, File}
import org.apache.maven.project.MavenProject


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/12/11
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */

object WrapperGenerator {

  def generate(targetDir: File, project: MavenProject) {

    var name = project.getArtifactId
    if (name.contains(".")) {
      name = name.substring(name.lastIndexOf(".") + 1)
    }
    val wrapperFile = new File(targetDir.getAbsolutePath + File.separator + project.getGroupId.replace(".", File.separator) + File.separator + project.getArtifactId.replace(".", File.separator) + File.separator + name + ".java")
    ZipHelper.createParentDirs(wrapperFile)

    val fw = new FileWriter(wrapperFile)
    fw.append("//Generated by Kevoree WebServer War Wrapper\n")
    fw.append("package " + project.getGroupId + "." + project.getArtifactId + ";\n")
    fw.append("import org.kevoree.library.javase.webserver.*;\n")
    fw.append("import org.kevoree.library.javase.webserver.servlet.LocalServletRegistry;\n")
    fw.append("import org.osgi.framework.Bundle;\n")

    fw.append("@org.kevoree.annotation.ComponentType\n")
    fw.append("public class " + name + " extends  org.kevoree.library.javase.webserver.AbstractPage {\n")

    fw.append("private org.kevoree.library.javase.webserver.servlet.LocalServletRegistry servletRepository = null;\n")

    fw.append("@Override\n")
    fw.append("public void startPage() {\n")
    fw.append("super.startPage();\n")
    fw.append("Bundle b = (Bundle)this.getDictionary().get(\"osgi.bundle\");")
    fw.append("servletRepository = new LocalServletRegistry(b);\n");

    fw.append("servletRepository.loadWebXml(this.getClass().getClassLoader().getResourceAsStream(\"web.xml\"));\n")
    fw.append("}//END START METHOD\n")

    fw.append("@Override\n")
    fw.append("public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {\n")

    fw.append("if (servletRepository.tryURL(request.getUrl(), request, response)) {return response;}\n")
    fw.append("if (FileServiceHelper.checkStaticFile(\"index.html\", this, request, response)) {return response;}\n")
    fw.append("response.setContent(\"Bad request\");\n")
    fw.append("return response;\n")

    fw.append("}//END process METHOD\n")


    fw.append("}\n")
    fw.close()
  }

}