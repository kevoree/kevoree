package org.kevoree.library.javase.accessControlGroup;

import org.kevoree.AccessControl.AccessControlRoot;
import org.kevoree.ContainerRoot;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/02/13
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class Tester {


    public static void main(String argv[]) throws Exception
    {

          /*
        for(int i=500;i<1000;i++){


            String type = "newtype"+i;
            String rule ="\n" +
                    "<elements xsi:type=\"AccessControl:Element\" elementQuery=\"typeDefinitions["+type+"]\">\n" +
                    "<permissions xsi:type=\"AccessControl:Permission\" PrimitiveQuery=\"StopInstance\"></permissions>\n" +
                    "<permissions xsi:type=\"AccessControl:Permission\" PrimitiveQuery=\"UpdateDictionaryInstance\"></permissions>\n" +
                    "<permissions xsi:type=\"AccessControl:Permission\" PrimitiveQuery=\"UpdateInstance\"></permissions>\n" +
                    "<permissions xsi:type=\"AccessControl:Permission\" PrimitiveQuery=\"StartInstance\"></permissions>\n" +
                    "<permissions xsi:type=\"AccessControl:Permission\" PrimitiveQuery=\"AddInstance\"></permissions>\n" +
                    "<permissions xsi:type=\"AccessControl:Permission\" PrimitiveQuery=\"RemoveInstance\"></permissions>\n" +
                    "</elements>\n" +
                    "\n";



            String t = "  addComponent Random"+i+"@node0 : Random {} \n";
            t +="            updateDictionary Random"+i+"@node0 \n";

            String filename= System.getProperty("java.io.tmpdir")+ File.separator+ "test.file";
            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            fw.write(t);
            fw.flush();
            fw.close();

            System.out.println(i);

        }

        System.exit(0);        */


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


        /*PUSH SIGNED MODEL
                // INSTALL PDP (MODEL ACCESS CONTROL)
        AccessControlRoot root = AccessControlXmiHelper.$instance.loadStream(Tester.class.getClassLoader().getResourceAsStream("benchmark/pdp/model4.ac"));
        System.out.println("PUSH PDP "+root.getUsers().size());

      group.pushPDP(root,rsaPrivateKey,KevScriptLoader.getModel(Tester.class.getClassLoader().getResource("benchmark/models2/emptynode.kevs").getPath()),"node0");

        Thread.sleep(5000); */
      ContainerRoot modeltarget = KevScriptLoader.getModel(Tester.class.getClassLoader().getResource("benchmark/models2/randomcomponent500.kevs").getPath());
        ContainerRoot modelempty = KevScriptLoader.getModel(Tester.class.getClassLoader().getResource("benchmark/models2/emptynode.kevs").getPath());
        for(int i=0;i<500;i++)
        {
            System.out.println("PUSH SIGNED MODEL "+i);
            group.pushSignedModel(rsaPrivateKey, modeltarget, "node0");
                    Thread.sleep(6000);
            System.out.println("PUSH SIGNED EMPTY MODEL "+i);
            group.pushSignedModel(rsaPrivateKey, modelempty, "node0");
        }


        System.exit(0);

    }

}

