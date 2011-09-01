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


import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;

import java.util.HashMap;

public abstract class AbstractGroupType {

    public abstract void triggerModelUpdate();

    public KevoreeModelHandlerService getModelService() {
        return null;
    }

    public HashMap<String, Object> getDictionary() {
        return null;
    } //OVERRIDE BY FACTORY

    public String getNodeName() {
        return null;
    }

    public String getName() {
        return null;
    }

}
