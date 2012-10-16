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
package org.kevoree.tools.nativeN.api;

import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public interface INativeGen {

    public int create_input(String name);
    public int create_output(String name);

    public String generateInputsPorts();
    public String generateOutputsPorts();

    public LinkedHashMap<String, Integer> getInputs_ports();
    public LinkedHashMap<String, Integer> getOuputs_ports();
    public String generateMethods();

}
