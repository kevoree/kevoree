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
package org.kevoree.library.defaultChannels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.*;

/**
 *
 * @author ffouquet
 */
@Library(name ="art2baselib")
@ChannelTypeFragment
public class defSERVICE extends AbstractChannelFragment {

    @Override
    public Object dispatch(Message msg) {
        Object result = null;
        if (this.getBindedPorts().size() == 1) {
            System.out.println("local dispatch");
            result = forward(getBindedPorts().get(0), msg);
        } else {
            if (this.getOtherFragments().size() == 1) {
                result = forward(getOtherFragments().get(0), msg);
            } else {
                System.out.println("No Art2 port or fragment bind on this channel fragment, message lost = "+msg.getContent());
            }
        }
        System.out.println("Ok result "+result);
        return result;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new NoopChannelFragmentSender();
    }
}
