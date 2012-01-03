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
package org.kevoree.framework;

import scala.Option;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 04/11/11
 * Time: 19:31
 * To change this template use File | Settings | File Templates.
 */
public interface KevoreeMessage extends java.io.Serializable {
    
    public KevoreeMessage putValue(String key, Object value);
    
    public Option<Object> getValue(String key);

    public List<String> getKeys();

    public void switchKey(String srcKey, String targetKey);

    //public boolean check();

}
