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

import java.util.jar.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author ffouquet
 */
public class PackageUtils {

    public static Set getFilteredPackageNames(String jarName, List<String> packageNames,boolean debug) {
        Set packages = new HashSet();

        List<String> protectedPackageName = new ArrayList<String>();
        for (String packageName : packageNames) {
            protectedPackageName.add(packageName.replaceAll("\\.", "/"));
            if (debug) {
                System.out.println("Jar " + jarName + " looking for " + packageName);
            }
        }

        try {
            JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if (jarEntry.getName().endsWith(".class")) {
                    for (String packagePrefixe : protectedPackageName) {
                        if (jarEntry.getName().startsWith(packagePrefixe)) {
                            String founded_packageName = jarEntry.getName().replaceAll("/", "\\.");
                            founded_packageName = founded_packageName.substring(0,founded_packageName.lastIndexOf("."));
                            founded_packageName = founded_packageName.substring(0,founded_packageName.lastIndexOf("."));
                            if (debug) {
                                System.out.println("Found " + founded_packageName);
                            }
                            packages.add(founded_packageName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packages;
    }
}
