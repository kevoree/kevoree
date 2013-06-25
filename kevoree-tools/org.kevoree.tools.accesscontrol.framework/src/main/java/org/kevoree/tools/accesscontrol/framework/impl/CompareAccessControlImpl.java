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
import org.kevoree.accesscontrol.*;
import org.kevoree.adaptation.accesscontrol.api.*;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.log.Log;
import org.kevoree.tools.accesscontrol.framework.api.ICompareAccessControl;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;
import sun.security.rsa.RSAPublicKeyImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
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
    private long start;
    private long duree;
    private boolean benchmark =false;
    private HashMap<String,Boolean> cache = new HashMap<String, Boolean>();

    public CompareAccessControlImpl(AccessControlRoot root){
        this.root = root;
    }

    public AccessControlRoot getRoot() {
        return root;
    }

    public void setRoot(AccessControlRoot root) {
        this.root = root;
    }

    @Override
    public List<AdaptationPrimitive> approval(String nodeName, ContainerRoot current_model, SignedModel target_modelSigned) throws ControlException {

        KevoreeKompareBean kompareBean = new KevoreeKompareBean();
        AdaptationModel adaptationModel = kompareBean.kompare(current_model,KevoreeXmiHelper.instance$.loadString(new String(target_modelSigned.getSerialiedModel())), nodeName);

        if(benchmark)
        {
            start= System.currentTimeMillis();
        }
        List<AdaptationPrimitive>  result = checker(adaptationModel,current_model, target_modelSigned);
        if(benchmark)
        {
            // HACK BENTCH
            duree =  (System.currentTimeMillis() - start) ;
            int size_rules = -1;
            User user = root.findUsersByID(target_modelSigned.getSignature().getKey());
            if(user != null){
                for(Role r :user.getRoles()){
                    size_rules += r.getElements().size();
                }
            }

            try
            {
                String filename= System.getProperty("java.io.tmpdir")+ File.separator+ "accesscontrol.benchmark";
                FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                fw.write(duree+ ";"+adaptationModel.getAdaptations().size()+";"+ size_rules+"\n");
                fw.close();
                Log.debug("UPDATE BENCHMARK FILE " + filename);
            }
            catch(IOException ioe)
            {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<AdaptationPrimitive> approval(AdaptationModel adaptationModel,ContainerRoot current_model, SignedModel target_model) throws ControlException {
        /// todo current model
        return checker(adaptationModel, current_model,target_model);
    }

    @Override
    public boolean accessPDP(SignedPDP p)
    {
        PDPSignature signature =  p.getSignature();
        User user = root.findUsersByID(signature.getKey());
        if(user == null){
            return false;
        }else {
            return user.getPdpadmin();
        }
    }

    public void setBenchmark(boolean benchmark) {
        this.benchmark = benchmark;
    }

    public  List<AdaptationPrimitive> checker(AdaptationModel adaptationModel,ContainerRoot currentmodel, SignedModel signedModel) throws ControlException
    {

        List<AdaptationPrimitive> result_forbidden = new ArrayList<AdaptationPrimitive>();
        if(root == null)
        {
            Log.error("No access control policy is defined.");
            throw new ControlException("No access control policy is defined.");
        }
        try
        {
            // todo          target_signed.getModelFormat()
            ContainerRoot target_model = KevoreeXmiHelper.instance$.loadString(new String(signedModel.getSerialiedModel()));

            ModelSignature signature =  signedModel.getSignature();
            Log.debug("Signature get key " + signature.getKey());
            Log.debug("Users = "+root.getUsers().size());

            User user = root.findUsersByID(signature.getKey());
            if(user == null){
                Log.warn("No user associated with the key");
                result_forbidden.addAll(adaptationModel.getAdaptations());
                return  result_forbidden;
            }

            if(user.getRoles().size() == 0)
            {
                Log.warn("No Role associated to this User");
                result_forbidden.addAll(adaptationModel.getAdaptations());
                return  result_forbidden;
            }

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
                    boolean allowed_cache = false;

                    if(cache.get(instance.getTypeDefinition().getName()) != null){
                        if(cache.get(instance.getTypeDefinition().getName()) == true){
                            allowed_cache =true;
                        }
                    }
                    if(!allowed_cache)
                    {
                        // this rules is not denied we check if is authorized
                        for(Role role : user.getRoles())
                        {
                            for(Element element :role.getElements())
                            {
                                Object ptr =   target_model.findByPath(element.getElementQuery());
                                if(ptr == null)
                                {
                                    // look in previous model maybe the component do no exist in the new
                                    ptr = currentmodel.findByPath(element.getElementQuery());
                                }

                                //      logger.debug("Query -->"+element.getElementQuery());

                                if( ptr != null && ptr instanceof TypeDefinition)
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
                         //   cache.put(((Instance) p.getRef()).getName(),found);
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
            }

        } catch (Exception e)
        {
            // this kPublicKey is ignore
            e.printStackTrace();
            return null;
        }

        return result_forbidden;

    }
}



