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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 13:35
 */
public class KevoreeKChannelConsumer extends DefaultConsumer {

    public KevoreeKChannelConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    public Object forwardMessage(Object msg) {
        Exchange exchange = getEndpoint().createExchange();
        exchange.getIn().setBody(msg);
        try {
            getProcessor().process(exchange);
            // log exception if an exception occurred and was not handled
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
            return exchange.getOut();
        } catch (Exception e) {
            getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            return null;
        }
    }

}
