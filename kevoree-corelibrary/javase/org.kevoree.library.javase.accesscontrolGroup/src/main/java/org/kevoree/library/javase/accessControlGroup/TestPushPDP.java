package org.kevoree.library.javase.accessControlGroup;

import org.kevoree.AccessControl.AccessControlRoot;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/03/13
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public class TestPushPDP {


    public static void main(String argv[]) throws Exception
    {

        HashMap<String, Object> dico = new HashMap<String, Object>();
        dico.put("port", "8080");
        dico.put("ip","localhost");
        dico.put("ssl","false");
        dico.put("gui","false");
        dico.put("pdp","false");

        AccessControlGroup group =new AccessControlGroup();
        group.setDictionary(dico);
        // private key
        String private_exponent = "4109406322895233351937244823949130450198126497340017617427663515773659616365455834584473049790061841196898489588297331922833138074446236327075996525971717609987352411769231643214939977856590128556711125769670219934822712525295744744260700314730439781770858314592005380741217371388959032631896022121650706113";
        String modulus = "144020407584804763735781397875483509259162896393675259140832504723667556298258224080835620462080899939316115674945584086752254208548119246078919563808881551818193159408718845506936985497165354139428760891323751580371321471610817626346638768300361018500421805148485036897404239717699245568771580543630086019231";

        RSAPrivateKey rsaPrivateKey = HelperSignature.getPrivateKey(modulus, private_exponent);
        //  System.out.println(Tester2.class.getClassLoader().getResourceAsStream("benchmark/pdp/model3.ac"));

        // INSTALL PDP (MODEL ACCESS CONTROL)
        AccessControlRoot root = AccessControlXmiHelper.$instance.loadStream(Tester.class.getClassLoader().getResourceAsStream("benchmark/pdp/model5.ac"));
        System.out.println("PUSH PDP "+root.getUsers().size());

        /*PUSH SIGNED MODEL      */
      group.pushPDP(root,rsaPrivateKey,KevScriptLoader.getModel(Tester.class.getClassLoader().getResource("benchmark/models2/emptynode.kevs").getPath()),"node0");
        System.exit(0);

    }
}
