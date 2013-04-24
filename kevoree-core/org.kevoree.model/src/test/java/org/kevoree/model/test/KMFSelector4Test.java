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
public class KMFSelector4Test {

    @Test
    public void testSelector() throws URISyntaxException {

        ModelLoader loader = new XMIModelLoader();
        ContainerRoot model = (ContainerRoot)loader.loadModelFromPath(new File(KMFSelector2Test.class.getResource("/deepModel.kev").toURI())).get(0);

        System.out.println(model.getNodes().size());

        //List<Object> result = model.selectByQuery("nodes[{components.name = *}]");
        List<Object> result = model.selectByQuery("nodes[{components.size=0}]");
        System.out.println("resultSize:"+result.size());

        assert(result.size() == 11);


        List<Object> result2 = model.selectByQuery("nodes[{components.name= Fake* }]");
        System.out.println("resultSize:"+result2.size());

        assert(result2.size() == 1);



    }




}
