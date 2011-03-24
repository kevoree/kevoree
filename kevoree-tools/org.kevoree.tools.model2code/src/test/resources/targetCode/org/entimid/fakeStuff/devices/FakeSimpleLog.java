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
package org.entimid.fakeStuff.devices;

import org.kevoree.annotation.*;

/**
 *
 * @author ffouquet
 */

@Provides({
    @ProvidedPort(name="pLog",type=PortType.MESSAGE)
})
@ComponentType
public class FakeSimpleLog extends AbstractFakeStuffComponent {

    @Port(name="pLog")
    public void processMsg(Object o){
        System.out.println("SimpleLog: "+o);
    }

    @Start
    public void start() {

    }

    @Stop
    public void stop() {
        
    }

   @Update
    public void update() {
        
    }

}
