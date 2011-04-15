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

import java.util.List;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sample.service.ServiceA;
import org.kevoree.library.sample.service.ServiceB;
import org.kevoree.library.sample.service.ServiceGenerics;

/**
 *
 * @author gnain
 */
@Provides({
    @ProvidedPort(name = "prov1", type = PortType.MESSAGE),
    @ProvidedPort(name = "prov2", type = PortType.SERVICE, className = ServiceA.class),
    @ProvidedPort(name = "req3", type = PortType.SERVICE, className = ServiceGenerics.class)
})
@Requires({
    @RequiredPort(name = "req1", type = PortType.MESSAGE),
    @RequiredPort(name = "req2", type = PortType.SERVICE, className = ServiceB.class)
})
@DictionaryType({
    @DictionaryAttribute(name = "mandatory1", optional = false),
    @DictionaryAttribute(name = "mandatory2", defaultValue = "DF", optional = false),
    @DictionaryAttribute(name = "optional1", optional = true),
    @DictionaryAttribute(name = "optional2", defaultValue = "DF", optional = true)
})
@NoneConcurrencyPorts({"prov1", "prov2"})
@Library(name = "Kevoree-Samples")
@ComponentType
public class ComponentA extends AbstractComponentType implements ServiceA {

    @Port(name = "req3",method="getMyList")
    public List<String> getMyList(){
        return null;
    }

    @Start
    public void start() {
        System.out.println("ComponentA::start() V5");

        for (String key : this.getDictionary().keySet()) {
            System.out.println("key=" + key + "=" + this.getDictionary().get(key));
        }

    }

    @Stop
    public void stop() {
        System.out.println("ComponentA::stop()");
    }

    @Port(name = "prov1")
    public void prov1Processor(Object o) {
        System.out.println("ComponentA::prov1Processor()");
    }

    @Port(name = "prov2", method = "methodAA")
    public String methodAA() {
        System.out.println("ComponentA::methodAA()");
        return "NOT NULL";
    }

    @Port(name = "prov2", method = "methodAB")
    public double methodAB() {
        System.out.println("ComponentA::methodAB()");
        return 2d;
    }
}
