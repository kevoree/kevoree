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
package org.kevoree.tools.accesscontrol.framework.api;


import org.kevoree.ContainerRoot;

import org.kevoree.adaptation.accesscontrol.api.ControlException;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.adaptation.accesscontrol.api.SignedPDP;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/01/13
 * Time: 17:24
 compares the  permissions to an access control
 */
public interface ICompareAccessControl
{

    /**
     * Call Kompare
     * @param nodeName
     * @param model
     * @param target
     * @return  The AdaptationPrimitive refused if is empty the access is approval
     */
    public List<AdaptationPrimitive> approval(String currentNodeName, ContainerRoot model, SignedModel target_model) throws ControlException;


    /**
     * don't call Kompare
     * @param adaptationModel
     * @param target_model
     * @return
     * @throws ControlException
     */
    public List<AdaptationPrimitive> approval(AdaptationModel adaptationModel,ContainerRoot currentmodel,SignedModel target_model) throws ControlException;

    public boolean accessPDP(SignedPDP p);


}
