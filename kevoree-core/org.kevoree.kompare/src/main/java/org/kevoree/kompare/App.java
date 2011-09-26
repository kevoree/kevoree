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
 *//*

package org.kevoree.kompare;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.*;

*/
/**
 * Hello world!
 *
 *//*

public class App {

    public static void main(String[] args) {
        System.out.println("Hello World!");


       // ContainerRoot amodel = KevoreeFactory.eINSTANCE.createContainerRoot();

        //ContainerNode aNode = KevoreeFactory.eINSTANCE.createContainerNode();
        //aNode.setName("duke");
        //amodel.getNodes().add(aNode);



        //ContainerRoot amodel = Art2Factory.eINSTANCE.createContainerRoot();
        ContainerRoot amodel = KevoreeXmiHelper.load("/Users/ffouquet/Downloads/model_fragment_binding1.kev");
        ContainerRoot nmodel = KevoreeXmiHelper.load("/Users/ffouquet/Downloads/model_fragment_binding2.kev");

        System.out.println("new model " + nmodel);


        KevoreeKompareBean kompareService = new KevoreeKompareBean();
        //Art2AdaptationDeployServiceOSGi adaptationService = new Art2AdaptationDeployServiceOSGi();
        //adaptationService.setContext(new Art2DeployManager());

        AdaptationModel adapModel = kompareService.kompare(amodel, nmodel, "duke");



        System.out.println("adaptationModel=>" + adapModel.getAdaptations().size());


        for (AdaptationPrimitive ap : adapModel.getAdaptations()) {
            System.out.println(ap.getClass().getSimpleName());

            if (ap.getRef() instanceof ) {
                System.out.println("ref=" + ((TypeAdaptation) ap).getRef().getName());
            }
            if (ap instanceof InstanceAdaptation) {
                System.out.println("ref=" + ((InstanceAdaptation) ap).getRef().getName());
            }
            if (ap instanceof BindingAdaptation) {
                System.out.println("ref=" + ((BindingAdaptation) ap).getRef().getHub().getName());
                System.out.println("ref=" + ((BindingAdaptation) ap).getRef().getPort().getPortTypeRef().getName() + "-" + ((ComponentInstance) ((BindingAdaptation) ap).getRef().getPort().eContainer()).getName());
            }
            if (ap instanceof FragmentBindingAdaptation) {
                System.out.println("ref=" + ((FragmentBindingAdaptation) ap).getTargetNodeName()+"-"+((FragmentBindingAdaptation) ap).getRef().getName());
            }


            // if (ap.getClass().getName().contains("ComponentType")) {
            //      toRemove.add(ap);
            //  }
        }

    }
}
*/
