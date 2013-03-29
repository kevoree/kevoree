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

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.NodeType;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.context.ContextFactory;
import org.kevoree.context.ContextRoot;
import org.kevoree.context.impl.DefaultContextFactory;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;

public abstract class
		AbstractNodeType extends AbstractTypeDefinition implements NodeType {

	public void startNode () {
	}

	public void stopNode () {
	}

	public void updateNode () {
	}

    @Override
    public void setNodeName(String pnodeName) {
        super.setNodeName(pnodeName);
        super.setName(pnodeName);
    }

    @Override
    public void setName(String pname) {
        super.setName(pname);
        super.setNodeName(pname);
    }

    public abstract AdaptationModel kompare (ContainerRoot actualModel, ContainerRoot targetModel);

	public abstract PrimitiveCommand getPrimitive (AdaptationPrimitive primitive);

    private ContextFactory contextFactory = new DefaultContextFactory();

	private ContextRoot contextModel = contextFactory.createContextRoot();

	@Override
	public ContextRoot getContextModel () {
		return contextModel;
	}

	/**
	 * Allow to find the corresponding element into the model
	 *
	 * @return the node corresponding to this
	 */
	public ContainerNode getModelElement () {
		return getModelService().getLastModel().findByPath("nodes[" + getName() + "]", ContainerNode.class);
	}

}




