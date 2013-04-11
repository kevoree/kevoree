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
import org.kevoree.*;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.loader.ModelLoader;
import org.kevoree.serializer.ModelSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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


    private void testClonerInternal(String fileName) throws URISyntaxException {

        System.out.println("input="+fileName);

        ModelLoader loader = new ModelLoader();
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot model = loader.loadModelFromPath(new File(ClonerTest.class.getResource("/"+fileName).toURI())).get(0);
        for(int i=0;i<400;i++){
            ContainerNode node = factory.createContainerNode();
            node.setName("node_"+i);
            model.addNodes(node);
        }

        for (TypeDefinition td : model.getTypeDefinitions()) {
            td.setRecursiveReadOnly();
        }
        for (DeployUnit du : model.getDeployUnits()) {
            du.setRecursiveReadOnly();
        }
        for (Repository r : model.getRepositories()) {
            r.setRecursiveReadOnly();
        }

        ModelCloner cloner = new ModelCloner();

        long heapSize = Runtime.getRuntime().freeMemory();

        //Normal Clone
        long before = System.currentTimeMillis();
        ContainerRoot modelCloned = cloner.clone(model);
        System.out.println("NormalClone = " + (System.currentTimeMillis() - before) + " ms");
        for (TypeDefinition td : modelCloned.getTypeDefinitions()) {
            assert (td.eContainer().equals(modelCloned));
        }

        long heapSizeAfterNormalClone = Runtime.getRuntime().freeMemory();

        System.out.println(heapSize - heapSizeAfterNormalClone);


        //Smart Clone
        long before2 = System.nanoTime();
        ContainerRoot modelCloned2 = cloner.cloneMutableOnly(model, false);
        System.out.println("SmartClone = " + (System.nanoTime() - before2) / Math.pow(10, 6) + " ms");
        for (TypeDefinition td : modelCloned2.getTypeDefinitions()) {
            assert (td.eContainer().equals(model));
            assert (model.findByPath(td.path()).equals(td));
        }

         System.out.println(modelCloned2.getTypeDefinitions().size());


        long heapSizeAfterSmartClone = Runtime.getRuntime().freeMemory();
        System.out.println(heapSizeAfterNormalClone - heapSizeAfterSmartClone);



        ByteArrayOutputStream s = new ByteArrayOutputStream();
        ModelSerializer saver = new ModelSerializer();
        saver.serialize(modelCloned2,s);

        ContainerNode newNode = factory.createContainerNode();
        newNode.setTypeDefinition(modelCloned2.findTypeDefinitionsByID("JavaSENode"));
        newNode.setName("newMutable");
        modelCloned2.addNodes(newNode);

    }

    @Test
    public void testModifiableContainementsRelationship() throws URISyntaxException {
        testClonerInternal("node0.kev");
    }

    @Test
    public void testModifiableContainementsRelationship2() throws URISyntaxException {
        testClonerInternal("mergedAll.kev");
    }


}
