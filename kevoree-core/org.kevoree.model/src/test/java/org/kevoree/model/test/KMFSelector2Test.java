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
import org.kevoree.ContainerRoot;
import org.kevoree.NodeType;
import org.kevoree.loader.ModelLoader;
import org.kevoree.loader.XMIModelLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 08/02/13
 * Time: 18:31
 */
public class KMFSelector2Test {

    @Test
    public void testSelector() throws URISyntaxException {

        ModelLoader loader = new XMIModelLoader();
        ContainerRoot model = loader.loadModelFromPath(new File(KMFSelector2Test.class.getResource("/bootstrapModel0.kev").toURI())).get(0);


        System.out.println(model.selectByQuery("typeDefinitions[*]"));


        List<Object> result = model.selectByQuery("typeDefinitions[{ &(name = *Node)(name = R*) }]");
        System.out.println("Result Size = " + result.size());
        for (Object o : result) {
            if (o instanceof org.kevoree.NamedElement) {
                System.out.println(((org.kevoree.NamedElement) o).getName());
            }
        }

        assert (result.size() == 1);
        assert (((NodeType) result.get(0)).getName().equals("RestNode"));


        ContainerRoot model2 = loader.loadModelFromPath(new File(KMFSelector2Test.class.getResource("/defaultlibs.kev").toURI())).get(0);
        List<Object> result2 = model2.selectByQuery("typeDefinitions[*]/provided[{name = on}]");
        System.out.println("Result Size = " + result2.size());
        for (Object o : result2) {
            if (o instanceof org.kevoree.NamedElement) {
                System.out.println(((org.kevoree.NamedElement) o).getName());
            } else {
                System.out.println("res=" + o);
            }
        }

        assert (result2.size() == 2);


    }


}
