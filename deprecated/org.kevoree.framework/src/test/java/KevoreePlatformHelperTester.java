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
import org.junit.Test;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreeXmiHelper;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/02/13
 * Time: 11:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class KevoreePlatformHelperTester {

    @Test
    public void testUpdateNodeLinkProp() {
        ContainerRoot model = KevoreeXmiHelper.instance$.loadStream(KevoreePlatformHelperTester.class.getResourceAsStream("/node0.kev"));
        KevoreePlatformHelper.instance$.updateNodeLinkProp(model, "sync", "node0", Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP(), "192.168.1.1", "LAN", 100);

    }
}
