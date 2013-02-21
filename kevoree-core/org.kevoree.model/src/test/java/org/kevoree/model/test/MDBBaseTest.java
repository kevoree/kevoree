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
package org.kevoree.model.test;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 20/02/13
* (c) 2013 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/


public class MDBBaseTest {


    public static void main(String[] args) {

        /*
        PersistentKevoreeFactory factory = new PersistentKevoreeFactory(new File("myModelDb"));

        System.out.println("Creation of ContainerRoot");
        ContainerRoot modelRoot = factory.createContainerRoot();

        for(int i = 0 ; i < 10; i++) {
            System.out.println("Creation of DeployUnit N°" + i);
            DeployUnit du = factory.createDeployUnit();
            System.out.println("SetID");
            du.setName("du"+ i);
            System.out.println("addToRoot");
            modelRoot.addDeployUnits(du);

            System.out.println("Creation of TypeDefinition N°" + i);
            TypeDefinition td = factory.createTypeDefinition();
            System.out.println("SetID");
            td.setName("td"+ i);
            System.out.println("SetDU");
            td.addDeployUnits(du);
            System.out.println("addToRoot");
            modelRoot.addTypeDefinitions(td);
        }

        assertTrue("Type definition list is not of expected size.", modelRoot.getTypeDefinitions().size()==10);
        assertTrue("Deploy Unit list is not of expected size.", modelRoot.getDeployUnits().size()==10);
        assertTrue("TypeDefinition do not have the good deploy unit.", modelRoot.getTypeDefinitions().get(5).getDeployUnits().get(0).getName().equals("du5"));
*/
    }

}
