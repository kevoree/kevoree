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
package org.kevoree.tools.control.framework.command;

import org.kevoree.KControlModel.KControlModelFactory;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.adaptation.control.api.ModelSignature;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.tools.control.framework.api.Command;
import org.kevoree.tools.control.framework.impl.ModelSignatureImpl;
import org.kevoree.tools.control.framework.utils.HelperSignature;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 11:08
 * This Command sign the model with the private key and store the signature and the publicKey
 */

public class CreateSignatureCommand implements Command {

    private SignedModel signedModel;
    private KeyPair key;

    public void setSignedModel(SignedModel signedModel)
    {
        this.signedModel = signedModel;
    }

    public void setKey(KeyPair key) {
        this.key = key;
    }

    @Override
    public void execute() throws ControlException {
        ModelSignature signature = null;
        try {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) key.getPublic();
            signature = new ModelSignatureImpl(HelperSignature.getSignature(key.getPrivate(), signedModel.getSerialiedModel()),rsaPublicKey.getPublicExponent()+":"+rsaPublicKey.getModulus());
            signedModel.getSignatures().add(signature);
        } catch (Exception e)
        {
          throw new ControlException(e);
        }
    }
}
