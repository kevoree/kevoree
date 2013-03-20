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
package org.kevoree.model.test;

import org.junit.Test;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.loader.ModelLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/02/13
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class KMFSelector3Test {

    @Test
    public void testSelector3() throws URISyntaxException {

        ModelLoader loader = new ModelLoader();
        ContainerRoot model = loader.loadModelFromPath(new File(KMFSelector2Test.class.getResource("/deepModel.kev").toURI())).get(0);

        /* Normal lookup */

        long beganS = System.nanoTime();
        ComponentInstance fConsole = null;
        for (ContainerNode node : model.getNodes()) {
            if (node.getName().equals("node6")) {
                for (ContainerNode node2 : node.getHosts()) {
                    if (node2.getName().equals("node7")) {
                        for (ContainerNode node3 : node2.getHosts()) {
                            if (node3.getName().equals("node8")) {
                                for (ContainerNode node4 : node3.getHosts()) {
                                    if (node4.getName().equals("node4")) {
                                        for (ComponentInstance i : node4.getComponents()) {
                                            if (i.getName().equals("FakeConso380")) {
                                                fConsole = i;break;
                                            }
                                        }break;
                                    }
                                }break;
                            }
                        }break;
                    }
                }break;
            }
        }
        System.out.println((System.nanoTime() - beganS) / 1000);
        System.out.println(fConsole.getName());
        System.out.println("-"+model.findByPath("nodes[node6]/hosts[node7]"));

        System.out.println(">"+model.selectByQuery("nodes[node6]/hosts[node7]"));


        assert (fConsole.getName().equals("FakeConso380"));
        assert (fConsole.path().equals("nodes[node4]/components[FakeConso380]"));


        long beganKMFQL = System.nanoTime();
        ComponentInstance fConsole2 = (ComponentInstance) model.findByPath("nodes[node6]/hosts[node7]/hosts[node8]/hosts[node4]/components[FakeConso380]");
        System.out.println((System.nanoTime() - beganKMFQL) / 1000);
        System.out.println(fConsole2.getName());

        assert (fConsole2.getName().equals("FakeConso380"));
        assert (fConsole2.path().equals("nodes[node4]/components[FakeConso380]"));


        System.out.println(fConsole2.path());

        List<Object> result = model.selectByQuery("typeDefinitions[{ &(name = *Node)(name = R*) }]");
        System.out.println("Result Size = " + result.size());
        for (Object o : result) {
            if (o instanceof org.kevoree.NamedElement) {
                System.out.println(((org.kevoree.NamedElement) o).getName());
            }
        }

    }

}
