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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.MoveComponentInstanceStatment

import scala.collection.JavaConversions._

import org.slf4j.LoggerFactory
import org.kevoree._
import tools.marShell.ast.MoveComponentInstanceStatment
import tools.marShell.interpreter.KevsInterpreterContext
import scala.Some

case class KevsMoveComponentInstanceInterpreter (moveComponent: MoveComponentInstanceStatment) extends KevsAbstractInterpreter {
	var logger = LoggerFactory.getLogger(this.getClass)

	def interpret (context: KevsInterpreterContext): Boolean = {

		moveComponent.cid.nodeName match {
			case Some(nodeID) => {
        context.model.findByQuery("nodes[" + nodeID + "]", classOf[ContainerNode]) match {
					case sourceNode:ContainerNode => {
						//SEARCH COMPONENT
            sourceNode.findByQuery("components[" + moveComponent.cid.componentInstanceName + "]", classOf[ComponentInstance]) match {
							case targetComponent:ComponentInstance => {
                context.model.findByQuery("nodes[" + moveComponent.targetNodeName + "]", classOf[ContainerNode])match {
									case targetNode:ContainerNode => {
										// look at all ports to get all channels and check attributes that are fragment dependent:
										// look at the fragment for sourceNode
										// if another component is bound to the channel we do nothing
										// else we need to remove the fragment
										// look at the fragment for targetNode
										// if another component is already bound to the channel we do nothing
										// else we need to copy the previous fragment
										var fragments = List[(Channel, Port, DictionaryValue)]()
										(targetComponent.getProvided ++ targetComponent.getRequired).foreach(p => p.getBindings.foreach(mb => {
											if (mb.getHub.getDictionary()!=null) {
												mb.getHub.getDictionary.getValues.foreach(dv => {
													if (dv.getAttribute.getFragmentDependant && dv.getTargetNode!=null && dv.getTargetNode.getName == nodeID) {
														fragments = fragments ++ List[(Channel, Port, DictionaryValue)]((mb.getHub, mb.getPort, dv))
													}
												})
											}
										}))
										if (fragments.size > 0) {
											fragments.foreach {
												tuple =>
													val sourceFragmentMustBeRemoved = !tuple._1.getBindings.exists(mb =>
														mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == tuple._2.eContainer.eContainer.asInstanceOf[ContainerNode].getName &&
																mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName != tuple._2.eContainer.asInstanceOf[ComponentInstance].getName)

													val targetFragmentMustBeAdded = !tuple._1.getDictionary.getValues
														.exists(odv => odv.getTargetNode!=null && odv.getTargetNode.getName == moveComponent.targetNodeName && odv.getAttribute.getName == tuple._3.getAttribute.getName)

//													println("sourceFragmentMustBeRemoved && targetFragmentMustBeAdded: " + sourceFragmentMustBeRemoved + "&&" + targetFragmentMustBeAdded)
													if (sourceFragmentMustBeRemoved && targetFragmentMustBeAdded) {
														tuple._3.setTargetNode(targetNode)
													} else if (targetFragmentMustBeAdded) {
														val value = KevoreeFactory.$instance.createDictionaryValue
														value.setAttribute(tuple._3.getAttribute)
														value.setTargetNode(targetNode)
														value.setValue(tuple._3.getValue)
														tuple._1.getDictionary.addValues(value)
													} else if (sourceFragmentMustBeRemoved) {
														tuple._1.getDictionary.removeValues(tuple._3)
													}
											}
										}
										sourceNode.removeComponents(targetComponent)
										targetNode.addComponents(targetComponent)
										true
									}
									case null => {
										logger.error("Target node not found " + moveComponent.cid.componentInstanceName)
										false
									}
								}
							}
							case null => {
								logger.error("Component not found " + moveComponent.cid.componentInstanceName)
								false
							}
						}
					}
					case null => {
						logger.error("Source Node not found " + nodeID)
						false
					}
				}
			}
			case None => false //TODO solve ambiguity
		}

	}
}