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

import org.kevoree.ContainerRoot;

import org.kevoree.Instance;
import org.kevoree.accesscontrol.AccessControlRoot;
import org.kevoree.adaptation.accesscontrol.api.ControlException;
import org.kevoree.adaptation.accesscontrol.api.Signed;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.adaptation.accesscontrol.api.SignedPDP;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.accesscontrol.framework.impl.CompareAccessControlImpl;
import org.kevoree.tools.accesscontrol.framework.impl.SignedModelImpl;
import org.kevoree.tools.accesscontrol.framework.impl.SignedPDPImpl;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoreeadaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 09/04/13
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAccessControlGroupType extends AbstractGroupType {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private AccessControlRoot root = null;
    private CompareAccessControlImpl accessControl;


    @Override
    public void push(ContainerRoot containerRoot, String s) throws Exception {
        // ignore
        logger.error("PUSH NOT SIGNED MODEL");
    }

    /**
     * Create PDP
     * @param root   The Access Control Model to PUSH
     * @param key    The Private Key to Sign the model
     * @return

     */
    protected SignedPDP createSignedPDP(AccessControlRoot root,PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        SignedPDP pdp = new SignedPDPImpl(root, key);
        return  pdp;
    }

    /**
     * Check if there is a current PDP available
     * @return   true if there is a pdp
     */
    protected boolean assertPDP(){
        if(root == null){
            return false;
        }
        return true;
    }
    protected SignedModel createSignedModel(ContainerRoot model,PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        SignedModel signedmodel = new SignedModelImpl(model, key);
        return signedmodel;
    }

    public AccessControlRoot getModelAccessControl() {
        return root;
    }

    public void setModelAccessControl(AccessControlRoot root) {
        this.root = root;
        accessControl = new CompareAccessControlImpl(root);
    }

    public abstract void pushPDP(ContainerRoot model,String targetNodeName, AccessControlRoot pdp,PrivateKey key) throws AccessControlException;
    public abstract void pushSignedModel(ContainerRoot model, String targetNodeName,PrivateKey key) throws AccessControlException;

    public CompareAccessControlImpl getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(CompareAccessControlImpl accessControl) {
        this.accessControl = accessControl;
    }

    public ContainerRoot getModel(SignedModel signedModel){
        return KevoreeXmiHelper.instance$.loadString(new String(signedModel.getSerialiedModel()));
    }

    public boolean approvalSignedModel(Object signed) throws ControlException {
        if (signed instanceof SignedModelImpl)
        {
            SignedModel signedModel = (SignedModelImpl) signed;
            if (assertPDP())
            {
                List<AdaptationPrimitive> result = getAccessControl().approval(getNodeName(), getModelService().getLastModel(), signedModel);
                if (result != null && result.size() == 0) {
                    logger.info("model accepted according to access control");
                    return true;
                } else {
                    if (result != null) {
                        for (AdaptationPrimitive p : result) {
                            String ref = "";
                            if (p.getRef() instanceof Instance) {
                                ref = ((Instance) p.getRef()).getTypeDefinition().getName();
                            } else {
                                ref = p.getRef().toString();
                            }
                            logger.error("Refused Adapation Primitive " + p.getPrimitiveType().getName() + " " + ref);
                        }
                    } else {
                        logger.error(" no result ");
                    }
                }
            } else {
                logger.error("There is no access control defined");
            }
        }
        return false;
    }

    public boolean approvalPDP(Object signed) throws ControlException {

        if (signed instanceof SignedPDPImpl) {
            SignedPDPImpl pdp = (SignedPDPImpl) signed;

            if (assertPDP()) {
                setModelAccessControl(AccessControlXmiHelper.instance$.loadString(new String(pdp.getSerialiedModel())));
                return true;
            } else
            {
                if (getAccessControl().accessPDP(pdp)) {
                    setModelAccessControl(AccessControlXmiHelper.instance$.loadString(new String(pdp.getSerialiedModel())));
                } else {
                    logger.error("There is no acess to PDP");
                    return true;
                }
            }

        }
        return false;
    }
}
