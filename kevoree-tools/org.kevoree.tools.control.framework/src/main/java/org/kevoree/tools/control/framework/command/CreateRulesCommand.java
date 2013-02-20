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
import org.kevoree.KControlModel.KControlRule;
import org.kevoree.KControlModel.KPublicKey;
import org.kevoree.KControlModel.RuleMatcher;
import org.kevoree.KControlModel.impl.DefaultKControlModelFactory;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.tools.control.framework.api.Command;
import org.kevoree.tools.control.framework.api.IAccessControlChecker;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
public class CreateRulesCommand implements Command {

    private IAccessControlChecker accessControl=null;
    private PublicKey publicKey;
    private HashMap<String,KControlRule> authorized_rules = new HashMap<String,KControlRule>();
    private HashMap<String,KControlRule> forbidden_rules = new HashMap<String,KControlRule>();

    private DefaultKControlModelFactory factory = new DefaultKControlModelFactory();

    public CreateRulesCommand(PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }

    public void setAccessControl(IAccessControlChecker accessControl)
    {
        this.accessControl = accessControl;
    }

    public KControlRule addAuthorizedMatcher(String _kElementQuery) throws ControlException {
        KControlRule rule=null;
        if(!authorized_rules.containsKey(_kElementQuery))
        {
            rule = factory.createKControlRule();
            rule.setKElementQuery(_kElementQuery);
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
        if( accessControl.getControlRoot().getKeys().contains(publicKey.getEncoded().toString()))
        {
            // already in model
            currentPublicKey =  accessControl.getControlRoot().findKeysByID(publicKey.getEncoded().toString());
        }else
        {
            //  no exist add

            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            currentPublicKey = factory.createKPublicKey();
            // todo we can do better
            currentPublicKey.setKey(rsaPublicKey.getPublicExponent()+":"+rsaPublicKey.getModulus());
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
            builder.append("\n" + kElementQuery + " \n---->");
            for(RuleMatcher r :       authorized_rules.get(kElementQuery).getMatcher()){
                builder.append(r.getPTypeQuery()+",");
            }
        }

        return builder.toString();
    }
}
