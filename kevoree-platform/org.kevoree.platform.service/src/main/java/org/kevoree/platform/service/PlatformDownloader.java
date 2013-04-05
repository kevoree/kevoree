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
package org.kevoree.platform.service;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.aether.framework.AetherUtil;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/02/13
 * Time: 11:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class PlatformDownloader {

    static String discoverVersion(String modelUrl) throws IOException {
        if (modelUrl.startsWith("mvn:")) {
            List<String> repos = new ArrayList<String>();
            String[] splittedUrl = modelUrl.substring("mvn:".length()).split("/");
            if ("RELEASE".equals(splittedUrl[2])) {
                repos.add("http://maven.kevoree.org/release/");
                splittedUrl[2] = "LATEST";
            } else {
                repos.add("http://maven.kevoree.org/snapshots/");
                repos.add("http://maven.kevoree.org/release/");
            }

            File modelFile = AetherUtil.instance$.resolveMavenArtifact(splittedUrl[1], splittedUrl[0], splittedUrl[2], repos);
            try {
                JarFile jar = new JarFile(modelFile);
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                return findVersionFromModel(jar.getInputStream(entry));
            } catch (IOException e) {
                // the file is not a JAR but maybe it is a Kevoree model or a Kevoree script
                return findVersionFromModel(new FileInputStream(modelFile));
            }
        } else if (modelUrl.startsWith("http:") || modelUrl.startsWith("https:")) {
            URL url = new URL(modelUrl);
            DataInputStream in = new DataInputStream(url.openStream());
            File file = File.createTempFile("modelFile", "");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);

            byte[] bytes = new byte[2048];
            int length = in.read(bytes);
            while (length != -1) {
                out.write(bytes, 0, length);
                length = in.read(bytes);
            }
            in.close();
            out.flush();
            out.close();

            try {
                JarFile jar = new JarFile(new File(modelUrl));
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                return findVersionFromModel(jar.getInputStream(entry));
            } catch (IOException e) {
                // the file is not a JAR but maybe it is a Kevoree model or a Kevoree script
                return findVersionFromModel(new FileInputStream(file));
            }
        } else {
            try {
                JarFile jar = new JarFile(new File(modelUrl));
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                return findVersionFromModel(jar.getInputStream(entry));
            } catch (IOException e) {
                // the file is not a JAR but maybe it is a Kevoree model or a Kevoree script
                return findVersionFromModel(new FileInputStream(new File(modelUrl)));
            }
        }
    }

    static  String download(String version) throws Exception {
        List<String> repos = new ArrayList<String>();
        if ("RELEASE".equals(version)) {
            repos.add("http://maven.kevoree.org/release/");
            version = "LATEST";
        } else {
            repos.add("http://maven.kevoree.org/snapshots/");
            repos.add("http://maven.kevoree.org/release/");
        }
        File jarFile = AetherUtil.instance$.resolveMavenArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", version, repos);

        if (jarFile.exists()) {
            return jarFile.getAbsolutePath();
        } else {
            throw new Exception("Unable to download platform with '" + version + "' as version");
        }
    }

    private static String findVersionFromModel(InputStream stream) {
        ContainerRoot model = KevoreeXmiHelper.instance$.loadStream(stream);
        if (model != null) {
            for (DeployUnit unit : model.getDeployUnits()) {
                if ("org.kevoree".equals(unit.getGroupName()) && "org.kevoree.framework".equals(unit.getUnitName())) {
                    return unit.getVersion();
                }
            }
        }
        return "LATEST";
    }
}
