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
import org.apache.camel.impl.DefaultComponent;
import org.kevoree.framework.AbstractComponentType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 14:45
 */
public class KevoreePortComponent extends DefaultComponent {

    AbstractComponentType c = null;
    public HashMap<String,KevoreePortConsumer> consumerInput = new HashMap<String,KevoreePortConsumer>();

    public KevoreePortComponent(AbstractComponentType ct){
        c = ct;
    }
    
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new KevoreePortEndpoint(c,remaining,this);
    }
}
