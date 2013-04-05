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

import org.kevoree.ContainerRoot;

import org.kevoree.adaptation.accesscontrol.api.ModelSignature;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.accesscontrol.framework.api.ModelFormat;
import org.kevoree.tools.accesscontrol.framework.impl.ModelSignatureImpl;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import java.io.Serializable;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 08:46
 */
public class SignedModelImpl implements SignedModel, Serializable {

    private ModelSignature signature;
    private ModelFormat currentFormat = ModelFormat.XMI;
    private byte[] rawmodel = null;

    public SignedModelImpl(ContainerRoot model, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        rawmodel = KevoreeXmiHelper.instance$.saveToString(model, false).getBytes();
        signature = new ModelSignatureImpl(HelperSignature.getSignature(key, getSerialiedModel()), ((RSAPrivateKey)key).getModulus().toString());
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
    public ModelSignature getSignature() {
        return signature;
    }

}
