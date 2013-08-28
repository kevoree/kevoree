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
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 14:57
 */
public class KevoreePortEndpoint extends ProcessorEndpoint {

    private AbstractComponentType componentType = null;
    private String portName = null;

    private KevoreePortComponent origin = null;

    public KevoreePortEndpoint(AbstractComponentType ct, String portName,KevoreePortComponent origin) {
        this.componentType = ct;
        this.portName = portName;
        this.origin = origin;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new DefaultProducer(this) {
            @Override
            public void process(Exchange exchange) throws Exception {
                if (componentType.isPortBinded(portName)) {
                    MessagePort mport = componentType.getPortByName(portName, MessagePort.class);
                    if (mport != null) {
                        mport.process(exchange.getIn().getBody());
                    }
                }
            }
        };
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        KevoreePortConsumer newc = new KevoreePortConsumer(this, processor);
        origin.consumerInput.put(portName, newc);
        return newc;
    }

    @Override
    protected String createEndpointUri() {
        return "kport:" + portName;
    }

}
