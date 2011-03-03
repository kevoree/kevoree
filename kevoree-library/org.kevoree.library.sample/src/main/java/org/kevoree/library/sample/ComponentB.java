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

package org.kevoree.library.sample;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sample.service.ServiceB;

/**
 *
 * @author gnain
 */

@Provides({
    @ProvidedPort(name="prov1", type=PortType.SERVICE, className=ServiceB.class)
})
@Requires({
    @RequiredPort(name="req2", type=PortType.MESSAGE),
    @RequiredPort(name="req3", type=PortType.MESSAGE)
})
@Library(name = "Kevoree-Samples")
@ComponentType
public class ComponentB extends AbstractComponentType implements ServiceB {

    @Port(name="prov1", method="methodBA")
    public void methodBA() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Port(name="prov1", method="methodBB")
    public boolean methodBB() {
        return true;
    }

        @Start
    public void start() {
        System.out.println("ComponentB::start()");
    }

    @Stop
    public void stop() {
        System.out.println("ComponentB::stop()");
    }

}
