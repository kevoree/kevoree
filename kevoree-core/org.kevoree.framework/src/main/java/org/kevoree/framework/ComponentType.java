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
package org.kevoree.framework;

import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author ffouquet
 */
public interface ComponentType {

    public void setHostedPorts(HashMap<String, Object> ports);

    public HashMap<String, Object> getHostedPorts();

    public void setNeededPorts(HashMap<String, Object> ports);

    public HashMap<String, Object> getNeededPorts();

    public HashMap<String,Object> getDictionary();

    public void setDictionary(HashMap<String,Object> dictionary);

    public <T> T getPortByName(String name, Class<T> type);

    public Boolean isPortBinded(String name);
    
    public String getNodeName();

    public String getName();
    
    
}
