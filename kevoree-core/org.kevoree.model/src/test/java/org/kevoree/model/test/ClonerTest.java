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
import org.kevoree.cloner.ModelCloner;
import org.kevoree.loader.ModelLoader;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/02/13
 * Time: 11:37
 */
public class ClonerTest {

    @Test
    public void testSelector() throws URISyntaxException {
        ModelLoader loader = new ModelLoader();
        ContainerRoot model = loader.loadModelFromPath(new File(ClonerTest.class.getResource("/node0.kev").toURI())).get(0);

        ModelCloner cloner = new ModelCloner();
        cloner.clone(model);


    }

}
