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
import java.util.Set;

/**
 *
 * @author ffouquet
 */
public class SysStaticClassGenerator {

    public static String generate(Set<String> packages){

        StringBuilder out = new StringBuilder();
        out.append("\n");
        out.append("package generated;\n");
        out.append("public class SysPackageConstants {\n");

        out.append("public static String getProperty(){\n");
        out.append("return \"\"");

        boolean first = true;
        for(String s : packages){
            out.append("+");
            if(!first){out.append("\",\"+");}
            first = false;
            out.append("\""+s+"\"\n");
        }

        out.append(";");

        out.append("}//end method \n");
        out.append("}//end class \n");

        return out.toString();
    }

    

}
