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


import org.kevoree.*;
import org.kevoree.AccessControl.*;
import org.kevoree.adaptation.accesscontrol.api.ControlException;
import org.kevoree.adaptation.accesscontrol.api.ModelSignature;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.tools.accesscontrol.framework.api.ICompareAccessControl;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import sun.security.rsa.RSAPublicKeyImpl;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/01/13
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class CompareAccessControlImpl implements ICompareAccessControl
{

    private AccessControlRoot root;

    public CompareAccessControlImpl(AccessControlRoot root){

        this.root = root;
    }
    @Override
    public List<AdaptationPrimitive> approval(String nodeName, ContainerRoot current_model, SignedModel target_modelSigned) throws ControlException {
        KevoreeKompareBean kompareBean = new KevoreeKompareBean();
        AdaptationModel adaptationModel = kompareBean.kompare(current_model,KevoreeXmiHelper.$instance.loadString(new String(target_modelSigned.getSerialiedModel())), nodeName);
        return checker(adaptationModel, target_modelSigned);
    }

    @Override
    public List<AdaptationPrimitive> approval(AdaptationModel adaptationModel, SignedModel target_model) throws ControlException {
        return checker(adaptationModel, target_model);
    }


    public  List<AdaptationPrimitive> checker(AdaptationModel adaptationModel, SignedModel signedModel) throws ControlException
    {
        if(root == null)
        {
            throw new ControlException("Access Control Model is not available");
        }

        List<AdaptationPrimitive> result_forbidden = new ArrayList<AdaptationPrimitive>();

        // todo          target_signed.getModelFormat()
        ContainerRoot target_model = KevoreeXmiHelper.$instance.loadString(new String(signedModel.getSerialiedModel()));

        ModelSignature signature =  signedModel.getSignature();

        try
        {
            User user = root.findUsersByID(signature.getKey());
            BigInteger exponent = new BigInteger(user.getPublicExponent());
            BigInteger modulus =  new BigInteger(user.getModulus());
            RSAPublicKey key = new RSAPublicKeyImpl(modulus,exponent);

            // check signature of the model
            if(!HelperSignature.verifySignature(signature.getSignature(), key, signedModel.getSerialiedModel()))
            {
                result_forbidden.addAll(adaptationModel.getAdaptations());
                return  result_forbidden;
            }

            for(AdaptationPrimitive p : adaptationModel.getAdaptations())
            {
                if(p.getRef() instanceof Instance)
                {
                    Instance instance =(Instance) p.getRef();
                    boolean  found = false;
                    // this rules is not denied we check if is authorized

                    for(Role role : user.getRoles())
                    {
                        for(Element element :role.getElements())
                        {
                            Object ptr =   target_model.findByPath(element.getElementQuery());

                            if( ptr instanceof TypeDefinition)
                            {
                                TypeDefinition componentType= (TypeDefinition) ptr;
                                if(instance.getTypeDefinition().getName().equals(componentType.getName()))
                                {
                                    if(element.findPermissionsByID(p.getPrimitiveType().getName()) != null)
                                    {
                                        found = true;
                                    }
                                }
                            }
                        }
                        if(!found)
                        {
                            if(!result_forbidden.contains(p)){
                                result_forbidden.add(p);
                            }
                        } else
                        {
                            if(result_forbidden.contains(p)){
                                result_forbidden.remove(p);
                            }
                        }

                    }
                }
            }

        } catch (Exception e)
        {
            // this kPublicKey is ignore
            e.printStackTrace();
        }

        return result_forbidden;

    }
}



