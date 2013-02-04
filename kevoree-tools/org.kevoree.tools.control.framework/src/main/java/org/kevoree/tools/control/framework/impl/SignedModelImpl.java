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
package org.kevoree.tools.control.framework.impl;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.control.api.ModelSignature;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.control.framework.utils.ModelFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public class SignedModelImpl implements SignedModel, Serializable {

    private List<ModelSignature> signatures;
    private ModelFormat currentFormat = ModelFormat.XMI;
    private byte[] rawmodel = null;

    public SignedModelImpl(ContainerRoot model) {
        signatures = new ArrayList<ModelSignature>();
        rawmodel = KevoreeXmiHelper.$instance.saveToString(model, false).getBytes();
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
    public List<ModelSignature> getSignatures() {
        return signatures;
    }

}
