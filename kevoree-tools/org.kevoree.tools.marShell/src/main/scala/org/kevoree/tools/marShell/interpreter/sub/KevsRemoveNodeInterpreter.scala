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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
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

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.tools.marShell.ast.{RemoveNodeStatment, AddNodeStatment}
import org.slf4j.LoggerFactory

case class KevsRemoveNodeInterpreter (addN: RemoveNodeStatment) extends KevsAbstractInterpreter {

	var logger = LoggerFactory.getLogger(this.getClass)

	def interpret (context: KevsInterpreterContext): Boolean = {

		context.model.getNodes.find(n => n.getName == addN.nodeName) match {
			case Some(targetNode) => {
				//DELETE ALL GROUP DEPENDENCY
				context.model.getGroups.foreach {
					g =>
						if (g.getSubNodes.contains(targetNode)) {
							g.removeSubNodes(targetNode)
						}
				}

				//DELETE ALL COMPONENT
				(targetNode.getComponents.toList ++ List()).foreach(c => {
					KevsRemoveComponentInstanceInterpreter(null).deleteComponent(targetNode, c)
				})

				//REMOVE FROM NETWORK LINK
				context.model.getNodeNetworks.foreach {
					nn =>
						if (nn.getTarget.getName == addN.nodeName) {
							context.model.removeNodeNetworks(nn)
						} else {
							nn.getInitBy.map {
								initNode =>
									if (initNode.getName == addN.nodeName) {
										context.model.removeNodeNetworks(nn)
									}
							}
						}

				}

				//CLEANUP HOST NODE
				context.model.getNodes.foreach {
					node =>
						node.getHosts.find(n => n.getName == addN.nodeName) match {
							case None =>
							case Some(n) =>
								node.removeHosts(n)
						}
				}

				//CLEANUP DICTIONARY
				import org.kevoree.framework.aspects.KevoreeAspects._
				context.model.getHubs.foreach {
					inst =>
						inst.getDictionary.map {
							dico =>
								dico.getValues.filter(v => v.getTargetNode.isDefined && v.getTargetNode.get.getName == addN.nodeName).foreach {
									value =>
										dico.removeValues(value)
								}
						}
				}
				context.model.getGroups.foreach {
					inst =>
						inst.getDictionary.map {
							dico =>
								dico.getValues.filter(v => v.getTargetNode.isDefined && v.getTargetNode.get.getName == addN.nodeName).foreach {
									value =>
										dico.removeValues(value)
								}
						}
				}

				//DELETE NODE
				context.model.removeNodes(targetNode)
				true
			}
			case None => {
				logger.error("Node Already existe")
				false
			}
		}
	}

}
