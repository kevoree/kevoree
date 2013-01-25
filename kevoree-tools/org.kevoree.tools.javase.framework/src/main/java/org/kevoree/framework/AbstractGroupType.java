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
package org.kevoree.framework;


import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.api.service.core.handler.ModelListener;

public abstract class AbstractGroupType extends AbstractTypeDefinition implements ModelListener {

    public abstract void triggerModelUpdate();

    public boolean triggerPreUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    public boolean triggerInitUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    public abstract void push(ContainerRoot model, String targetNodeName) throws Exception;

    public abstract ContainerRoot pull(String targetNodeName) throws Exception;


    /**
     * Allow to find the corresponding element into the model
     * Be careful, this method use the KevoreeModelHandlerService#getLastModel but this method is locked in some cases
     *
     * @return the group corresponding to this
     */
    public Group getModelElement() {
        return getModelService().getLastModel().findByQuery("groups[" + getName() + "]", Group.class);
    }

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return triggerPreUpdate(currentModel, proposedModel);
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return triggerInitUpdate(currentModel, proposedModel);
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public void modelUpdated() {
        triggerModelUpdate();
    }

    @Override
    public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }

    @Override
    public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }
}
