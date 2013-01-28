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
/* $Id: NodePropertyEditor.java 13934 2010-12-19 21:56:29Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.property;

import com.explodingpixels.macwidgets.HudWidgetFactory;
import com.explodingpixels.macwidgets.plaf.HudComboBoxUI;
import org.kevoree.*;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.AsyncCommandWrapper;
import org.kevoree.tools.ui.editor.command.SynchNodeTypeCommand;
import org.kevoree.tools.ui.editor.command.UpdatePhysicalNode;
import org.kevoree.tools.ui.editor.widget.JCommandButton;
import javax.swing.*;

/**
 * @author ffouquet
 */
public class NodePropertyEditor extends InstancePropertyEditor {

	public NodePropertyEditor (final Instance elem, KevoreeUIKernel _kernel) {

		super(elem, _kernel);

		final ContainerNode node = (ContainerNode) elem;

		final UpdatePhysicalNode commandUpdate = new UpdatePhysicalNode();
		commandUpdate.setKernel(_kernel);
		commandUpdate.setTargetCNode(node);


		/*
			 JTable table = new JTable(new InstanceTableModel(node));
			 JScrollPane scrollPane = new JScrollPane(table);
			 table.setFillsViewportHeight(true);

			 scrollPane.setPreferredSize(new Dimension(300, 150));

			 this.addCenter(scrollPane);
				*/

		this.addCenter(new NetworkPropertyEditor(node));

		final SynchNodeTypeCommand sendNodeType = new SynchNodeTypeCommand(true);
		final SynchNodeTypeCommand receiveNodeType = new SynchNodeTypeCommand(false);


		sendNodeType.setKernel(_kernel);
		sendNodeType.setDestNodeName(node.getName());

		DefaultComboBoxModel groupModel = new DefaultComboBoxModel();
		for (Group g : _kernel.getModelHandler().getActualModel().getGroups()) {
			if (g.getSubNodes().contains(node)) {
				groupModel.addElement(g.getName());
			}
		}
		final JComboBox groupTypeComboBox = new JComboBox(groupModel);
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setEnabled(false);
		final JLabel resultLabel = HudWidgetFactory.createHudLabel("");

		sendNodeType.setProgressBar(progressBar);
		sendNodeType.setResultLabel(resultLabel);

		final JCheckBox checkBox = HudWidgetFactory.createHudCheckBox("AMerge");

		JCommandButton btPushNodeType = new JCommandButton("Push") {
			@Override
			public void doBeforeExecution () {
				if (groupTypeComboBox.getSelectedItem() != null) {
					progressBar.setEnabled(true);
					progressBar.setIndeterminate(true);
					resultLabel.setText("Connect...");
					sendNodeType.setDestNodeName(elem.getName());
					sendNodeType.setViaGroupName(groupTypeComboBox.getSelectedItem().toString());
					sendNodeType.setAutoMerge(checkBox.isSelected());
				} else {
					resultLabel.setText("No group found !");
				}
			}
		};
		receiveNodeType.setKernel(_kernel);
		receiveNodeType.setDestNodeName(node.getName());
		receiveNodeType.setProgressBar(progressBar);
		receiveNodeType.setResultLabel(resultLabel);

		JCommandButton btPullNodeType = new JCommandButton("Pull") {
			@Override
			public void doBeforeExecution () {
				if (groupTypeComboBox.getSelectedItem() != null) {
					progressBar.setEnabled(true);
					progressBar.setIndeterminate(true);
					resultLabel.setText("Sending...");
					receiveNodeType.setDestNodeName(elem.getName());
					receiveNodeType.setViaGroupName(groupTypeComboBox.getSelectedItem().toString());
					receiveNodeType.setAutoMerge(checkBox.isSelected());
				} else {
					resultLabel.setText("No group found !");
				}
			}
		};

		AsyncCommandWrapper pushAsync = new AsyncCommandWrapper(sendNodeType);
		btPushNodeType.setCommand(pushAsync);
		AsyncCommandWrapper pullAsync = new AsyncCommandWrapper(receiveNodeType);
		btPullNodeType.setCommand(pullAsync);

		groupTypeComboBox.setUI(new HudComboBoxUI());
		JPanel layout = new JPanel();
		layout.setOpaque(false);
		layout.setLayout(new SpringLayout());


		JPanel subLayout = new JPanel();
		subLayout.setOpaque(false);
		subLayout.setLayout(new BoxLayout(subLayout, BoxLayout.LINE_AXIS));
		subLayout.add(checkBox);
		subLayout.add(btPushNodeType);
		subLayout.add(btPullNodeType);

		layout.add(subLayout);
		layout.add(groupTypeComboBox);

		layout.add(progressBar);
		layout.add(resultLabel);


		SpringUtilities.makeCompactGrid(layout, 2, 2, 6, 6, 6, 6);
		this.addCenter(layout);

	}
}
