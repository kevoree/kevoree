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

import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/01/12
 * Time: 16:35
 */
public interface ArduinoGenerator {

    public void declareStaticKMessage(String name, String typeName);

    public void appendNativeStatement(String statement);

    public void freeStaticKMessage(String name);

    public void sendKMessage(String name,String portName);

    public void razGen();

    public String getContent();

    public TypeDefinition getTypeModel();

    public void setTypeModel(TypeDefinition td);

}


