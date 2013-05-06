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
package org.kevoree.platform.standalone;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/02/12
 * Time: 17:46
 */
public class Runner {

    public static void main( String[] args ) throws Exception {

        
     //   System.setProperty("kevoree.offline","true");
        System.setProperty("node.bootstrap", args[0]);
        System.setProperty("node.name", "node0");
        //System.setProperty("node.log.level","DEBUG");
        //System.setProperty("node.update.timeout","30000");
        App.main(args);
    }

}
