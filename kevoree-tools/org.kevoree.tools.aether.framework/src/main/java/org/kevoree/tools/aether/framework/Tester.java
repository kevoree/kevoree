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
package org.kevoree.tools.aether.framework;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.DefaultKevoreeFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 2/4/13
 * Time: 9:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Tester {

    public static void main(String[] args){
        NodeTypeBootstrapHelper boot = new NodeTypeBootstrapHelper();
        ContainerRoot model = KevoreeXmiHelper.instance$.load("/Users/duke/Documents/dev/dukeboard/kevoree-kotlin/kevoree-corelibrary/model/org.kevoree.library.model.bootstrap/target/classes/KEV-INF/lib.kev");

        ContainerNode node0 = new DefaultKevoreeFactory().createContainerNode();
        node0.setName("node0");
        node0.setTypeDefinition(model.findTypeDefinitionsByID("JavaSENode"));
        model.addNodes(node0);

        System.out.println(boot.bootstrapNodeType(model,"node0",null,null));




    }

}
