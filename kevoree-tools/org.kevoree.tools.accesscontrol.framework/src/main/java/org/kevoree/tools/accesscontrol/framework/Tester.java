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

import org.kevoree.accesscontrol.*;
import org.kevoree.accesscontrol.impl.DefaultAccessControlFactory;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;


import org.kevoree.adaptation.accesscontrol.api.SignedModel;


import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.JavaSePrimitive;
import org.kevoree.tools.accesscontrol.framework.api.ICompareAccessControl;

import org.kevoree.tools.accesscontrol.framework.impl.CompareAccessControlImpl;

import org.kevoree.tools.accesscontrol.framework.impl.SignedModelImpl;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import org.kevoreeadaptation.AdaptationPrimitive;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
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



        ContainerRoot current_model = KevoreeXmiHelper.instance$.loadStream(Tester.class.getClassLoader().getResourceAsStream("empty_node.kev"));
        ContainerRoot target_model = KevoreeXmiHelper.instance$.loadStream(Tester.class.getClassLoader().getResourceAsStream("random_nio_grapher_group.kev"));
        AccessControlRoot root = AccessControlXmiHelper.instance$.loadStream(Tester.class.getClassLoader().getResourceAsStream("model.ac"));


        ICompareAccessControl accessControl =    new CompareAccessControlImpl(root);

        String modulus = ("144020407584804763735781397875483509259162896393675259140832504723667556298258224080835620462080899939316115674945584086752254208548119246078919563808881551818193159408718845506936985497165354139428760891323751580371321471610817626346638768300361018500421805148485036897404239717699245568771580543630086019231");
        String private_exponent = ("4109406322895233351937244823949130450198126497340017617427663515773659616365455834584473049790061841196898489588297331922833138074446236327075996525971717609987352411769231643214939977856590128556711125769670219934822712525295744744260700314730439781770858314592005380741217371388959032631896022121650706113");


        SignedModel signedmodel = new SignedModelImpl(target_model,  HelperSignature.getPrivateKey(modulus, private_exponent));

        List<AdaptationPrimitive> result =  accessControl.approval("node0", current_model, signedmodel);

        if(result.size() == 0)
        {
            System.out.println("accepted");
        }else
        {
            for(AdaptationPrimitive p : result)
            {
                System.err.println("ERROR "+p.getPrimitiveType().getName()+" "+p.getRef());
            }
        }







    }

}
