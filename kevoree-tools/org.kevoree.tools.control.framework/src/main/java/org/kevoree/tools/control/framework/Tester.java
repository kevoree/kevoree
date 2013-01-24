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
package org.kevoree.tools.control.framework;

import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.KControlModel.KControlRule;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.JavaSePrimitive;
import org.kevoree.tools.control.framework.api.IAccessControlChecker;
import org.kevoree.tools.control.framework.command.CreateRulesCommand;
import org.kevoree.tools.control.framework.command.CreateSignatureCommand;
import org.kevoree.tools.control.framework.impl.SignedModelImpl;
import org.kevoree.tools.control.framework.utils.HelperSignature;
import org.kevoree.tools.control.framework.utils.HelperMatcher;
import org.kevoreeAdaptation.AdaptationPrimitive;

import java.security.KeyPair;
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

        ContainerRoot current_model = KevoreeXmiHelper.loadStream(Tester.class.getClassLoader().getResourceAsStream("empty_node.kev"));
        ContainerRoot target_model = KevoreeXmiHelper.loadStream(Tester.class.getClassLoader().getResourceAsStream("random_nio_grapher_group.kev"));



        IAccessControlChecker accessControl = ControlFactory.createAccessControlChecker();


        CreateRulesCommand rules = new CreateRulesCommand(key1.getPublic());
        rules.setAccessControl(accessControl);

        KControlRule r1 = rules.addAuthorizedMatcher("typeDefinitions[FakeConsole]");
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r2 = rules.addAuthorizedMatcher("typeDefinitions[BasicGroup]");
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));


        KControlRule r3 = rules.addAuthorizedMatcher("typeDefinitions[Grapher]");
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r4 = rules.addAuthorizedMatcher( "typeDefinitions[NioChannel]");
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));


   //     System.out.println(rules);

        rules.execute();


        SignedModel signedmodel = new SignedModelImpl(target_model);

        // create a signature
        CreateSignatureCommand   c = new CreateSignatureCommand();
        c.setSignedModel(signedmodel);
        c.setKey(key1);
        c.execute();

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
