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
package org.kevoree.tools.accesscontrol.framework;

import org.kevoree.AccessControl.*;
import org.kevoree.AccessControl.impl.DefaultAccessControlFactory;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;

import org.kevoree.KevoreeFactory;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;

import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.JavaSePrimitive;
import org.kevoree.tools.accesscontrol.framework.api.ICompareAccessControl;

import org.kevoree.tools.accesscontrol.framework.impl.CompareAccessControlImpl;

import org.kevoree.tools.accesscontrol.framework.impl.SignedModelImpl;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import org.kevoreeAdaptation.AdaptationPrimitive;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/01/13
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
public class Tester {

    public static void main(String argv[]) throws Exception
    {

        KeyPair key1 =  HelperSignature.generateKeys(1024);

        ContainerRoot current_model = KevoreeXmiHelper.$instance.loadStream(Tester.class.getClassLoader().getResourceAsStream("empty_node.kev"));
        ContainerRoot target_model = KevoreeXmiHelper.$instance.loadStream(Tester.class.getClassLoader().getResourceAsStream("random_nio_grapher_group.kev"));


        DefaultAccessControlFactory factory = new DefaultAccessControlFactory();

        AccessControlRoot root =  factory.createAccessControlRoot();

        Role role1 = factory.createRole();

        User user1 = factory.createUser();
        user1.setModulus(((RSAPublicKey)key1.getPublic()).getModulus().toString());   //id
        user1.setPublicExponent(((RSAPublicKey) key1.getPublic()).getPublicExponent().toString());



        root.addUsers(user1);


        Element element1 = factory.createElement();
        element1.setElementQuery("typeDefinitions[FakeConsole]");

        Permission p1 = factory.createPermission();
        p1.setPrimitiveQuery(JavaSePrimitive.AddInstance());
        Permission p2 = factory.createPermission();
        p2.setPrimitiveQuery(JavaSePrimitive.StartInstance());
        Permission p3 = factory.createPermission();
        p3.setPrimitiveQuery(JavaSePrimitive.UpdateInstance());
        Permission p4 = factory.createPermission();
        p4.setPrimitiveQuery(JavaSePrimitive.StopInstance());
        Permission p5 = factory.createPermission();
        p5.setPrimitiveQuery(JavaSePrimitive.UpdateDictionaryInstance());
        Permission p6 = factory.createPermission();
        p6.setPrimitiveQuery(JavaSePrimitive.AddFragmentBinding());

        element1.addPermissions(p1);
        element1.addPermissions(p2);
        element1.addPermissions(p3);
        element1.addPermissions(p4);
        element1.addPermissions(p5);

        Element element2 = factory.createElement();
        element2.setElementQuery("typeDefinitions[NioChannel]");


        //element2.addPermissions(p1);
        element2.addPermissions(p2);
        element2.addPermissions(p3);
        element2.addPermissions(p4);
        element2.addPermissions(p5);
        element2.addPermissions(p6);



        Element element3 = factory.createElement();
        element3.setElementQuery("typeDefinitions[Grapher]");
        element3.addAllPermissions(HelperSignature.getGenericsPermissions());


        Element element4 = factory.createElement();
        element4.setElementQuery("typeDefinitions[BasicGroup]");
        element4.addAllPermissions(HelperSignature.getGenericsPermissions());



        role1.addElements(element1);
        role1.addElements(element2);
        role1.addElements(element3);
        role1.addElements(element4);



        user1.addRoles(role1);

        ICompareAccessControl accessControl =    new CompareAccessControlImpl(root);





        SignedModel signedmodel = new SignedModelImpl(target_model,key1.getPrivate());



        List<AdaptationPrimitive> result =     accessControl.approval("node0", current_model, signedmodel);

        if(result.size() == 0)
        {
            System.out.println("accepted");
        }else
        {
            for(AdaptationPrimitive p : result)
            {
                System.err.println("ERROR "+p.getPrimitiveType().getName()+" "+((Instance)p.getRef()).getName());
            }
        }







    }

}
