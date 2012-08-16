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
package org.kevoree.library.camel.framework;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 13:26
 */
public abstract class AbstractKevoreeCamelChannelType extends AbstractChannelFragment {

    private CamelContext context = null;

    public CamelContext getContext() {
        return context;
    }

    public CamelContext buildCamelContext() {
        return new DefaultCamelContext();
    }

    private KevoreeChannelDispatcherComponent cc = null;

    @Start
    public void startCamelChannel() throws Exception {
        final AbstractKevoreeCamelChannelType selfPointer = this;
        context = buildCamelContext();
        context.setClassResolver(new ClassLoaderClassResolver(selfPointer.getClass().getClassLoader()));
        cc = new KevoreeChannelDispatcherComponent(selfPointer);
        context.addComponent("kchannel", cc);
        RouteBuilder rb = new RouteBuilder() {
            public void configure() {
                buildRoutes(this);
            }
        };
        context.addRoutes(rb);
        context.start();
    }

    @Stop
    public void stopCamelChannel() throws Exception {
        if (context != null) {
            context.stop();
        }
        cc = null;
        context = null;
    }

    @Update
    public void updateCamelChannel() throws Exception {
        stopCamelChannel();
        startCamelChannel();
    }

    protected abstract void buildRoutes(RouteBuilder rb);

    @Override
    public Object dispatch(Message msg) {
        if (cc.consumerInput!=null) {
            return cc.consumerInput.forwardMessage(msg);
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String s, String s1) {
        return new NoopChannelFragmentSender();
    }

}
