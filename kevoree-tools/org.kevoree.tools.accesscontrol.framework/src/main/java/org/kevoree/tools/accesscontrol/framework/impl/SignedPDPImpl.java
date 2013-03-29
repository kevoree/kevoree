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
package org.kevoree.tools.accesscontrol.framework.impl;

import org.kevoree.accesscontrol.AccessControlRoot;
import org.kevoree.adaptation.accesscontrol.api.ModelSignature;
import org.kevoree.adaptation.accesscontrol.api.PDPSignature;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.adaptation.accesscontrol.api.SignedPDP;
import org.kevoree.tools.accesscontrol.framework.api.ModelFormat;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/02/13
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class SignedPDPImpl implements SignedPDP, Serializable {


    private PDPSignatureImpl signature;
    private ModelFormat currentFormat = ModelFormat.XMI;
    private byte[] rawmodel = null;

    public  SignedPDPImpl(AccessControlRoot root,PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        rawmodel = AccessControlXmiHelper.$instance.saveToString(root, false).getBytes();
        signature = new PDPSignatureImpl(HelperSignature.getSignature(key, getSerialiedModel()), ((RSAPrivateKey)key).getModulus().toString());
    }

    @Override
    public byte[] getSerialiedModel() {
        return rawmodel;
    }

    @Override
    public String getModelFormat() {
        return currentFormat.name();
    }

    @Override
    public PDPSignature getSignature() {
        return signature;
    }

}
