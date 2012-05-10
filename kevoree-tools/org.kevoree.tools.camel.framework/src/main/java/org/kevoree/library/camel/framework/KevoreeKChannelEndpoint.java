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

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.impl.ProcessorEndpoint;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 13:40
 */
public class KevoreeKChannelEndpoint extends ProcessorEndpoint {

    private AbstractKevoreeCamelChannelType componentType = null;
    private String fragName = null;
    private KevoreeChannelDispatcherComponent origin = null;

    public KevoreeKChannelEndpoint(AbstractKevoreeCamelChannelType ct, String fragName,KevoreeChannelDispatcherComponent origin) {
        this.componentType = ct;
        this.fragName = fragName;
        this.origin = origin;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new DefaultProducer(this) {
            @Override
            public void process(Exchange exchange) throws Exception {
                /*if (componentType.isPortBinded(portName)) {
                    MessagePort mport = componentType.getPortByName(portName, MessagePort.class);
                    if (mport != null) {
                        mport.process(exchange.getIn().getBody());
                    }
                }*/



            }
        };
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        if(origin.consumerInput == null){
            KevoreeKChannelConsumer newc = new KevoreeKChannelConsumer(this, processor);
            origin.consumerInput = newc;
        }
        return origin.consumerInput;
    }

    @Override
    protected String createEndpointUri() {
        return "kchannel:" + fragName;
    }

}
