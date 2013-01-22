package org.kevoree.tools.control.framework.command;

import org.kevoree.KControlModel.KControlModelFactory;
import org.kevoree.KControlModel.KControlRule;
import org.kevoree.KControlModel.KPublicKey;
import org.kevoree.KControlModel.RuleMatcher;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.tools.control.framework.api.Command;
import org.kevoree.tools.control.framework.api.IAccessControl;
import sun.security.rsa.RSAPublicKeyImpl;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
public class CreateRulesCommand implements Command {

    private IAccessControl accessControl=null;
    private PublicKey publicKey;
    private HashMap<String,KControlRule> authorized_rules = new HashMap<String,KControlRule>();
    private HashMap<String,KControlRule> forbidden_rules = new HashMap<String,KControlRule>();

    public CreateRulesCommand(PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }

    public void setAccessControl(IAccessControl accessControl)
    {
        this.accessControl = accessControl;
    }

    public KControlRule addAuthorizedMatcher(String _kElementQuery) throws ControlException {
        KControlRule rule=null;
        if(!authorized_rules.containsKey(_kElementQuery))
        {
            rule = KControlModelFactory.$instance.createKControlRule();
            rule.set_kElementQuery(_kElementQuery);
            authorized_rules.put(_kElementQuery,rule);
        }  else
        {
            rule = authorized_rules.get(_kElementQuery);
        }
        return rule;
    }

    @Override
    public void execute() throws ControlException {
        if(accessControl == null)
        {
            // throw exception
            throw new ControlException(" IAccessControl is null");
        }

        KPublicKey currentPublicKey;
        if( accessControl.getControlRoot().get_keys().keySet().contains(publicKey.getEncoded().toString()))
        {
            // already in model
            currentPublicKey =  accessControl.getControlRoot().get_keys().get(publicKey.getEncoded().toString());
        }else
        {
            //  no exist add

            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            currentPublicKey = KControlModelFactory.$instance.createKPublicKey();
            // todo we can do better
            currentPublicKey.set_key(rsaPublicKey.getPublicExponent()+":"+rsaPublicKey.getModulus());
            accessControl.getControlRoot().addKeys(currentPublicKey);
        }

        //  authorized_rules
        for(String kElementQuery : authorized_rules.keySet()){
            currentPublicKey.addAuthorized(authorized_rules.get(kElementQuery));
        }
        //forbidden_rules
        for(String kElementQuery : forbidden_rules.keySet()){
            currentPublicKey.addAuthorized(authorized_rules.get(kElementQuery));
        }

    }


    public String toString(){
        StringBuilder  builder =new StringBuilder();
        for(String kElementQuery : authorized_rules.keySet()){
            builder.append("\n"+kElementQuery+" \n---->");
            for(RuleMatcher r :       authorized_rules.get(kElementQuery).get_matcher()){
                builder.append(r.getPTypeQuery()+",");
            }
        }

        return builder.toString();
    }
}
