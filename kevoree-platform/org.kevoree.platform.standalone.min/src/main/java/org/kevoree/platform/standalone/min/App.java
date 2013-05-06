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
package org.kevoree.platform.standalone.min;

import org.kevoree.platform.standalone.KevoreeBootStrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 13/07/12
 * Time: 01:39
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        Long startTime = System.currentTimeMillis();
        System.setProperty("kcl.lazy", "true");
        KevoreeBootStrap.nodeBootClass = "org.kevoree.tools.aether.framework.min.MinNodeTypeBootstrapHelper";
        if (System.getProperty("node.groupType") == null) {
            System.setProperty("node.groupType", "BasicGroup");
        }
        KevoreeBootStrap.logService = new SimpleServiceKevLog();

        String node_name = System.getProperty("node.name");
        if (node_name == null || node_name.equals("")) {
            node_name = "node0";
            System.setProperty("node.name", node_name);
        }
        final KevoreeBootStrap kb = new KevoreeBootStrap();

        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    kb.stop();
                } catch (Throwable ex) {
                }
            }
        });
        kb.start();
        logger.info("Kevoree runtime boot time {} ms", (System.currentTimeMillis() - startTime));
    }

}
