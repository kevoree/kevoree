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


public interface ModelListener {

    /**
     * Method called before Kevoree Core accept an input model. Synchronized this methods is not suppose to block
     *
     * @param context
     * @return
     */
    public boolean preUpdate(UpdateContext context);

    /**
     * Method called to prepare the core to be update. Synchronized this methods can bloc Kevoree core
     *
     * @param context
     * @return
     */
    public boolean initUpdate(UpdateContext context);

    /* Method called after the local update of the runtime. Synchronized this method can bloc Kevoree core and must return true if update is accepted or not if there is any failure  */
    public boolean afterLocalUpdate(UpdateContext context);

    /**
     * Method called asynchronisly after a model update
     */
    public void modelUpdated();


    public void preRollback(UpdateContext context);

    public void postRollback(UpdateContext context);

}
