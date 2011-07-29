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
package test;

import TestProject.src.main.java.test.TestIt;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * @author Gregory NAIN
 */
@Provides({
        @ProvidedPort(name = "onoff", type = PortType.SERVICE),
        @ProvidedPort(name = "onoffok", type = PortType.SERVICE,className = TestIt.class)
})
@RequiredPort(name="req1", type = PortType.SERVICE)
@ComponentType
public class VoidServicePortType extends AbstractComponentType {

    public void start() {

    }

    public void stop() {
    }

    public void update() {
        //TODO check new values in dictionnary
    }

    @Ports({
       @Port(name = "onoff", method = "on")
    })
    public void on(Object o) {
    }

}
