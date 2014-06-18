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
package org.kevoree.platform.standalone.gui;

import org.kevoree.impl.DefaultKevoreeFactory;

import javax.swing.*;

public class PromptApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        String s = (String) JOptionPane.showInputDialog(
                null,
                "Enter a node name and the Group port",
                "Kevoree Runtime Bootstrap",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "node0:9000");

        if(s!= null){

            String[] parts = s.split(":");
            if(parts.length == 2){
                String nodeName = parts[0];
                String portName = parts[1];

                StringBuilder buffer = new StringBuilder();
                buffer.append("repo \"http://oss.sonatype.org/content/groups/public/\"\n");

                if(new DefaultKevoreeFactory().getVersion().toLowerCase().contains("snapshot")){
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:latest\n");
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:latest\n");
                } else {
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:release\n");
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:release\n");
                }

                buffer.append("add "+nodeName+" : JavaNode\n");
                buffer.append("add sync : WSGroup\n");
                buffer.append("attach "+nodeName+" sync\n");
                buffer.append("set sync.port/"+nodeName+" = \""+portName+"\"");
                final KevoreeGUIFrame frame = new KevoreeGUIFrame(buffer.toString(),nodeName);
            }

        }
    }

}