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
package org.kevoree.api.handler;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/02/12
 * Time: 14:05
 */
public interface LockCallBack {

    /**
     * This method is called when a lock has been required on Kevoree Core.
     * If <b>bypassUUID</b> is different than <b>null</b> and <b>error</b> is not true so the lock is correctly acquired and you can use the uuid to apply reconfiguration.
     * @param bypassUUID The uuid which allow to apply reconfiguration.
     * @param error a boolean which explicit if the lock is correctly set
     */
    void run(UUID bypassUUID, Boolean error);

}
