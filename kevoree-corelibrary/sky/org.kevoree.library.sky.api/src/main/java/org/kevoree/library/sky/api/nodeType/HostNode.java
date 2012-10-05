package org.kevoree.library.sky.api.nodeType;

import org.kevoree.annotation.PrimitiveCommands;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/10/12
 * Time: 17:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@PrimitiveCommands(value = {}, values = {HostNode.REMOVE_NODE, HostNode.ADD_NODE})
public interface HostNode {
	public static final String REMOVE_NODE = "RemoveNode";
	public static final String ADD_NODE = "AddNode";
}
