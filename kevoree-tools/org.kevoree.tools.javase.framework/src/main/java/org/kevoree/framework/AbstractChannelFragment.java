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
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.framework;

import org.kevoree.Channel;
import org.kevoree.framework.message.Message;

/**
 * @author ffouquet
 */
public abstract class AbstractChannelFragment extends AbstractTypeDefinition implements ChannelFragment {

    public ChannelTypeFragmentThread delegate = null;

    public java.util.List<KevoreePort> getBindedPorts() {
        return delegate.getBindedPorts();
    }

    public java.util.List<KevoreeChannelFragment> getOtherFragments() {
        return delegate.getOtherFragments();
    }

    public Object forward(KevoreePort d, Message msg) {
        return delegate.forward(d, msg);
    }

    public Object forward(KevoreeChannelFragment d, Message msg) {
        return delegate.forward(d, msg);
    }

    public Object remoteDispatch(Message msg) {
        return delegate.dispatch(msg);
    }

    /**
     * Allow to find the corresponding element into the model
     *
     * @return the channel corresponding to this
     */
    public Channel getModelElement() {
        return getModelService().getLastModel().findByPath("hubs[" + getName() + "]", Channel.class);
    }

}
