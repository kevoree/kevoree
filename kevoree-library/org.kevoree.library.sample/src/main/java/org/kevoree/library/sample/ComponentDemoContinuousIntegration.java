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

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Port;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.ProvidedPort;
import org.kevoree.annotation.Provides;
import org.kevoree.annotation.RequiredPort;
import org.kevoree.annotation.Requires;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 *
 * @author gnain
 */
@Provides({
    @ProvidedPort(name = "In", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "Out", type = PortType.MESSAGE)
})
@DictionaryType({
    @DictionaryAttribute(name = "chaine", optional = false)
})

@Library(name = "Kevoree-Samples")
@ComponentType
public class ComponentDemoContinuousIntegration extends AbstractComponentType {


	String chaine;
	
    @Start
    public void start() {
        System.out.println("ComponentDemoContinuousIntegration::start() V5");
        for (String key : this.getDictionary().keySet()) {
            System.out.println("key=" + key + "=" + this.getDictionary().get(key));
        }
        chaine =  (String) this.getDictionary().get("chaine");

    }

    @Stop
    public void stop() {
        System.out.println("ComponentDemoContinuousIntegration::stop()");
    }

    @Update
    public void update() {
        System.out.println("ComponentDemoContinuousIntegration::update()");
        chaine =  (String) this.getDictionary().get("chaine");
    }
    
    @Port(name = "In")
    public void prov1Processor(Object o) {
        forward("" + o);
    }

    private void forward(String msg) {
        if (this.isPortBinded("Out")) {
            this.getPortByName("Out", MessagePort.class).process(chaine + msg.toUpperCase());
        }
    }
 
}
