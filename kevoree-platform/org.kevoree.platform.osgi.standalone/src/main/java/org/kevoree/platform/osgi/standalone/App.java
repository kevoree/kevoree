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
package org.kevoree.platform.osgi.standalone;

import org.slf4j.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public void start() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if(System.getProperty("node.log.appender.file") != null){
            System.out.println("Kevoree log will out in file => "+System.getProperty("node.log.appender.file"));
        } else {
            root.detachAppender("FILE");
        }
        EmbeddedFelix felix = new EmbeddedFelix();
        felix.run();
        try {
            felix.getM_fwk().waitForStop(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    public static void main(String[] args) {

        ConstantsHandler.setConstantValuesProvider(new ConstantValuesImpl());

        App app = new App();
        app.start();

    }
}
