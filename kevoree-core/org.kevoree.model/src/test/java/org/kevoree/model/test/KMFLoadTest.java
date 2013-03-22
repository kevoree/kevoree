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

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/03/13
 * Time: 11:29
 */
public class KMFLoadTest {

    @Test
    public void testLoad() throws URISyntaxException {

        ModelLoader loader = new ModelLoader();
        ModelCloner cloner = new ModelCloner();
        ContainerRoot model = loader.loadModelFromPath(new File(ClonerTest.class.getResource("/node0.kev").toURI())).get(0);

        long before = System.currentTimeMillis();
        for(int i=0;i<10000;i++){
            ContainerRoot model2 = cloner.clone(model);
            model2.delete();
        }

        long after = System.currentTimeMillis();

        System.out.println("Time : "+(after-before)+" ms");
        MemoryMXBean mbs = ManagementFactory.getMemoryMXBean();
        System.out.println(mbs.getObjectPendingFinalizationCount());
        List<GarbageCollectorMXBean> mbs2 = ManagementFactory.getGarbageCollectorMXBeans();
        System.out.println(mbs2.get(0).getCollectionTime());



    }


}
