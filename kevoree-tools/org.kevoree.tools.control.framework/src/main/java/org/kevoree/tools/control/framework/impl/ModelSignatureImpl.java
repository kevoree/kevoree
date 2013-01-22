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

import org.kevoree.adaptation.control.api.ModelSignature;import java.lang.Override;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/21/13
 * Time: 4:10 PM
 */
public class ModelSignatureImpl implements ModelSignature {

    String _key;
    byte[] _signature = null;

    public ModelSignatureImpl(byte[] s,String key)
    {
        _signature = s;
        this._key = key;
    }

    @Override
    public String getKey() {
        return _key;
    }

    @Override
    public byte[] getSignature() {
        return _signature;
    }

}
