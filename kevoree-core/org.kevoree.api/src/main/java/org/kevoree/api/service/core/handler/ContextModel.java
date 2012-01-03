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
package org.kevoree.api.service.core.handler;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/01/12
 * Time: 11:25
 */
public interface ContextModel {
    /**
     * @param key Key must unique and without special chars
     *            In case of periodic value, a timestamp can added at the end
     *            ex : node42.channels43.kb.{timestamp}
     * @return
     *             Return value is a plain byte array
     *             Byte interpretation must be done by requester
     */
    public byte[] get(ContextKey key);

    /**
     * @param key Key must unique and without special chars
     *            Key can be partial and can contain * to express multiplicity
     *            ex : node42.channels43.load.*
     * @return
     *             Return a map of selected values and keys
     *             Byte interpretation must be done by requester
     */
    public Map<ContextKey,byte[]> select(ContextKey key);

    /**
     * ex : node42.channels43.kb.{timestamp}
     * @param key Key must unique and without special chars
     *            In case of periodic value, a timestamp can added at the end
     *            ex : node42.channels43.kb.{timestamp}
     * @param value Value must be plain byte array
     */
    public void put(ContextKey key, byte[] value);


    

}
