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
package org.kevoree.tools.accesscontrol.framework.utils;


import org.kevoree.accesscontrol.impl.DefaultAccessControlFactory;
import org.kevoree.kompare.JavaSePrimitive;

import java.math.BigInteger;
import java.security.*;
import java.security.Permission;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/01/13
 * Time: 17:29
 * To change this template use File | Settings | File Templates.
 */
public class HelperSignature {

    public static byte[] getSignature(PrivateKey privateKey,byte []model) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(privateKey);
        sig.update(model);
        return sig.sign();
    }


    public static boolean verifySignature(byte[]signatureBytes ,PublicKey key,byte []model) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(key);
        sig.update(model);
        if(sig.verify(signatureBytes)){
            return  true;
        }
        return false;
    }


    public static List<  org.kevoree.accesscontrol.Permission> getGenericsPermissions(){
        DefaultAccessControlFactory factory = new DefaultAccessControlFactory();
        ArrayList<  org.kevoree.accesscontrol.Permission> p = new ArrayList<  org.kevoree.accesscontrol.Permission>();
        org.kevoree.accesscontrol.Permission p1 = factory.createPermission();
        p1.setPrimitiveQuery(JavaSePrimitive.instance$.getAddInstance());
        org.kevoree.accesscontrol.Permission p2 = factory.createPermission();
        p2.setPrimitiveQuery(JavaSePrimitive.instance$.getStartInstance());
        org.kevoree.accesscontrol.Permission p3 = factory.createPermission();
        p3.setPrimitiveQuery(JavaSePrimitive.instance$.getUpdateInstance());
        org.kevoree.accesscontrol.Permission p4 = factory.createPermission();
        p4.setPrimitiveQuery(JavaSePrimitive.instance$.getStopInstance());
        org.kevoree.accesscontrol.Permission p5 = factory.createPermission();
        p5.setPrimitiveQuery(JavaSePrimitive.instance$.getUpdateDictionaryInstance());
        org.kevoree.accesscontrol.Permission p6 = factory.createPermission();
        p6.setPrimitiveQuery(JavaSePrimitive.instance$.getAddFragmentBinding());
        p.add(p1);
        p.add(p2);
        p.add(p3);
        p.add(p4);

        p.add(p5);
        p.add(p6);
        return   p;

    }

    public static KeyPair generateKeys(int size) throws NoSuchAlgorithmException{
        // Generate a key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(size);
        KeyPair kp = kpg.genKeyPair();
        return kp;
    }


    public static RSAPrivateKey getPrivateKey(String _modulus,String _private_exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger modulus = new BigInteger(_modulus);
        BigInteger private_exponent =new BigInteger(_private_exponent);
        KeySpec spec =	  new RSAPrivateKeySpec(modulus,private_exponent);
        RSAPrivateKey key = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        return  key;
    }

    public static String serializePublicKey(PublicKey key){
        return "{"+((RSAPublicKey)key).getPublicExponent()+":"+((RSAPublicKey)key).getModulus()+"}";
    }

}
