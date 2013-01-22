package org.kevoree.tools.control.framework.impl;


import org.kevoree.*;
import org.kevoree.KControlModel.*;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.adaptation.control.api.ModelSignature;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.tools.control.framework.api.IAccessControl;
import org.kevoree.tools.control.framework.utils.HelperSignature;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import sun.security.rsa.RSAPublicKeyImpl;

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
public class AccessControlImpl implements IAccessControl
{
    private KControlRoot controlRoot=null;

    public KControlRoot getControlRoot() {
        return controlRoot;
    }

    public void setControlRoot(KControlRoot controlRoot) {
        this.controlRoot = controlRoot;
    }

    public AccessControlImpl()
    {
        this.controlRoot = KControlModelFactory.$instance.createKControlRoot();
    }

    @Override
    public List<AdaptationPrimitive> approval(String nodeName, ContainerRoot current_model, SignedModel target_modelSigned) throws ControlException {
        KevoreeKompareBean kompareBean = new KevoreeKompareBean();
        AdaptationModel adaptationModel = kompareBean.kompare(current_model,KevoreeXmiHelper.loadString(new String(target_modelSigned.getSerialiedModel())), nodeName);
        return docontrol(adaptationModel, target_modelSigned);
    }

    @Override
    public List<AdaptationPrimitive> approval(AdaptationModel adaptationModel, SignedModel target_model) throws ControlException
    {
        return docontrol(adaptationModel, target_model);
    }

    public  List<AdaptationPrimitive> docontrol(AdaptationModel adaptationModel, SignedModel signedModel) throws ControlException
    {
        if(controlRoot == null)
        {
            throw new ControlException("no control model");
        }
        List<AdaptationPrimitive> result_forbidden = new ArrayList<AdaptationPrimitive>();
        List<KControlRule> authorize_rules = new ArrayList<KControlRule>();
        List<KControlRule> forbidden_rules = new ArrayList<KControlRule>();


        // todo          target_signed.getModelFormat()
        ContainerRoot target_model = KevoreeXmiHelper.loadString(new String(signedModel.getSerialiedModel()));

        // <public key> <sign model>
        HashMap<byte[],ModelSignature> tuple_key_sign =  signedModel.getSignatures();

        //get the Kcontrol with our publickey
        for( byte[] pk: tuple_key_sign.keySet())
        {
            for(KPublicKey kPublicKey : controlRoot.getKeys())
            {
                try
                {
                    // todo we can do better
                    BigInteger exponent = new BigInteger(kPublicKey.get_key().split(":")[0]);
                    BigInteger modulus = new BigInteger(kPublicKey.get_key().split(":")[1]);
                    RSAPublicKey key = new RSAPublicKeyImpl(modulus,exponent);
                    // check signature of the model
                    if(HelperSignature.verifySignature(tuple_key_sign.get(pk).getSignature(), key, signedModel.getSerialiedModel()))
                    {
                        if(kPublicKey.get_key().getBytes().equals(pk))
                        {
                            authorize_rules.addAll(kPublicKey.get_authorized());
                            forbidden_rules.addAll(kPublicKey.get_forbidden());
                        }

                    }
                } catch (Exception e)
                {
                    // this kPublicKey is ignore
                    e.printStackTrace();
                }
            }
        }


        for(AdaptationPrimitive p : adaptationModel.getAdaptationsForJ())
        {
            if(p.getRef() instanceof Instance)
            {
                Instance instance =(Instance) p.getRef();

                boolean  found_in_refused_rules = false;
                boolean  found_in_authorize_rules = false;

                // check in refused rules
                for(KControlRule rule :authorize_rules)
                {
                    Object ptr =   target_model.findByQuery( rule.getKElementQuery());


                    if( ptr instanceof TypeDefinition)
                    {
                        TypeDefinition componentType= (TypeDefinition) ptr;
                        if(instance.getTypeDefinition().getName().equals(componentType.getName()))
                        {
                            /// check Matchers
                            for(RuleMatcher m : rule.get_matcher())
                            {
                                if(m.getPTypeQuery().equals(p.getPrimitiveType().getName())){
                                    found_in_refused_rules = true;
                                    break;
                                }

                            }
                        }
                    }
                }

                if(found_in_refused_rules)
                {
                    if(!result_forbidden.contains(p))
                    {
                        result_forbidden.add(p);
                    }
                } else
                {
                    // this rules is not denied we check if is authorized
                    for(KControlRule rule :authorize_rules)
                    {

                        Object ptr =   target_model.findByQuery(rule.getKElementQuery());

                        if( ptr instanceof TypeDefinition)
                        {
                            TypeDefinition componentType= (TypeDefinition) ptr;


                            if(instance.getTypeDefinition().getName().equals(componentType.getName())){

                                /// check Matchers
                                for(RuleMatcher m : rule.get_matcher())
                                {
                                    if(m.getPTypeQuery().equals(p.getPrimitiveType().getName())){
                                        found_in_authorize_rules = true;
                                        break;
                                    }

                                }
                            }
                        }

                    }
                    if(!found_in_authorize_rules)
                    {
                        if(!result_forbidden.contains(p)){
                            result_forbidden.add(p);
                        }
                    }

                }



            } else  if(p.getRef() instanceof MBinding){





            }



        }

        return result_forbidden;

    }
}


