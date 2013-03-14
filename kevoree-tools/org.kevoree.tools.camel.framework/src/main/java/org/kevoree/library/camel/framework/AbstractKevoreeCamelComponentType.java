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
import org.kevoree.framework.AbstractComponentType;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 11:50
 */

@Library(name = "JavaSE")
@ComponentFragment
public abstract class AbstractKevoreeCamelComponentType extends AbstractComponentType {

    private CamelContext context = null;

    public CamelContext getContext() {
        return context;
    }

    public CamelContext buildCamelContext() {
        return new DefaultCamelContext();
    }

    private KevoreePortComponent cc = null;

    @Start
    public void start() throws Exception {

        final AbstractKevoreeCamelComponentType selfPointer = this;
        context = buildCamelContext();
        //context.setClassResolver(new ClassLoaderClassResolver(selfPointer.getClass().getClassLoader()));
        cc = new KevoreePortComponent(selfPointer);
        context.addComponent("kport", cc);
        RouteBuilder rb = new RouteBuilder() {
            public void configure() {
                buildRoutes(this);
            }
        };
        context.addRoutes(rb);
        context.start();
    }

    @Stop
    public void stop() throws Exception {
        if (context != null) {
            context.stop();
        }
        cc = null;
        context = null;
    }

    @Update
    public void update() throws Exception {
        stop();
        start();
    }

    protected abstract void buildRoutes(RouteBuilder rb);

    @Port(name = "*")
    public void globalInput(Object o, String pname) {
        if (cc.consumerInput.containsKey(pname)) {
            cc.consumerInput.get(pname).forwardMessage(o);
        }
    }

}
