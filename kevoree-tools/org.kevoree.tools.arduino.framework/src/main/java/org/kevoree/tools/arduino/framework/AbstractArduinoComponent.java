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
package org.kevoree.tools.arduino.framework;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/01/12
 * Time: 16:15
 */
@ComponentFragment
public abstract class AbstractArduinoComponent extends AbstractComponentType {

    @Start
    @Stop
    @Update
    public void dummy() {
    }

    @Generate("header")
    public void generateHeader(ArduinoGenerator gen) {

    }

    @Generate("classheader")
    public void generateClassHeader(ArduinoGenerator gen) {

    }

    @Generate("classinit")
    public void generateInit(ArduinoGenerator gen) {

    }

    @Generate("classdestroy")
    public void generateDestroy(ArduinoGenerator gen) {

    }


}
