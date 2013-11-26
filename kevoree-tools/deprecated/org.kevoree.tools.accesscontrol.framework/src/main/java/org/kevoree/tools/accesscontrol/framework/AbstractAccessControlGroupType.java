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
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.log.Log;
import org.kevoree.tools.accesscontrol.framework.impl.CompareAccessControlImpl;
import org.kevoree.tools.accesscontrol.framework.impl.SignedModelImpl;
import org.kevoree.tools.accesscontrol.framework.impl.SignedPDPImpl;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;
import org.kevoreeadaptation.AdaptationPrimitive;

import javax.swing.*;
import java.io.*;
import java.security.PrivateKey;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 09/04/13
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAccessControlGroupType extends AbstractGroupType {

    private CompareAccessControlImpl accessControl;


    @Override
    public void push(ContainerRoot model, String node) throws Exception {
        String private_exponent = "";
        String modulus = "";

        JFileChooser dialogue = new JFileChooser(new File("."));
        PrintWriter sortie;
        File fichier = null;
        if (dialogue.showOpenDialog(null) ==
                JFileChooser.APPROVE_OPTION) {
            fichier = dialogue.getSelectedFile();
            sortie = new PrintWriter
                    (new FileWriter(fichier.getPath(), true));

            sortie.close();
        }
        FileReader fr = new FileReader(fichier);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder stringkey = new StringBuilder();
        try {
            String line = br.readLine();

            while (line != null) {
                stringkey.append(line);
                line = br.readLine();
            }

            br.close();
            fr.close();
            private_exponent = stringkey.toString().split(":")[0];
            modulus = stringkey.toString().split(":")[1];
            pushSignedModel(model,node,HelperSignature.getPrivateKey(modulus, private_exponent));
        } catch (EOFException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AccessControlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }


    public abstract void pushPDP(ContainerRoot model,String targetNodeName, AccessControlRoot pdp,PrivateKey key) throws AccessControlException;
    public abstract void pushSignedModel(ContainerRoot model, String targetNodeName,PrivateKey key) throws AccessControlException;


    public AccessControlRoot getModelAccessControl() {
        return accessControl.getRoot();
    }

    private void setModelAccessControl(AccessControlRoot root) {
        accessControl = new CompareAccessControlImpl(root);
    }


    private CompareAccessControlImpl getAccessControl() {
        return accessControl;
    }

    private void setAccessControl(CompareAccessControlImpl accessControl) {
        this.accessControl = accessControl;
    }

    private ContainerRoot getModel(SignedModel signedModel){
        return KevoreeXmiHelper.instance$.loadString(new String(signedModel.getSerialiedModel()));
    }

    private boolean approvalSignedModel(Object signed) throws ControlException {
        if (signed instanceof SignedModelImpl)
        {
            SignedModel signedModel = (SignedModelImpl) signed;
            if (getAccessControl().getRoot() != null)
            {
                List<AdaptationPrimitive> result = getAccessControl().approval(getNodeName(), getModelService().getLastModel(), signedModel);
                if (result != null && result.size() == 0) {
                    Log.info("model accepted according to access control");
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
                            Log.error("Refused Adapation Primitive " + p.getPrimitiveType().getName() + " " + ref);
                        }
                    } else {
                        Log.error(" no result ");
                    }
                }
            } else {
                Log.error("There is no access control defined");
            }
        }
        return false;
    }

    private boolean approvalPDP(Object signed) throws ControlException {

        if (signed instanceof SignedPDPImpl) {
            SignedPDPImpl pdp = (SignedPDPImpl) signed;

            if (getAccessControl() == null) {
                setModelAccessControl(AccessControlXmiHelper.instance$.loadString(new String(pdp.getSerialiedModel())));
                Log.debug("Successful installation of the PDP");
                return true;
            } else
            {

                if (getAccessControl().accessPDP(pdp)) {
                    setModelAccessControl(AccessControlXmiHelper.instance$.loadString(new String(pdp.getSerialiedModel())));
                } else {
                    Log.error("There is no acess to PDP");
                    return true;
                }
            }

        }
        return false;
    }

    /**
     *
     * @param m    SignedPDP or SignedModel
     * @throws ControlException
     */

    protected void updateSignedModel(final Object m) throws ControlException {

        if(approvalPDP(m)){
            Log.debug("accepted PDP");
        }

        if(approvalSignedModel(m)) {
            Log.debug("accepted Model");

            new Thread() {
                public void run() {
                    try {
                        long duree, start;
                        getModelService().unregisterModelListener(AbstractAccessControlGroupType.this);
                        start = System.currentTimeMillis();
                        getModelService().atomicUpdateModel(getModel((SignedModel) m));
                        duree = (System.currentTimeMillis() - start);
                        getModelService().registerModelListener(AbstractAccessControlGroupType.this);
                    } catch (Exception e) {
                        Log.error("", e);
                    }
                }
            }.start();
        }

    }

}
