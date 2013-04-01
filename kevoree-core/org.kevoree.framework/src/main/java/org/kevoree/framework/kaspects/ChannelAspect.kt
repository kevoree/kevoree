package org.kevoree.framework.kaspects

import java.util.ArrayList
import org.kevoree.Channel
import org.kevoree.ContainerNode
import org.kevoree.MBinding

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/03/13
 * Time: 08:45
 */

class ChannelAspect() {

    /**
* Returns true if the node in parameter hosts a component bound to this channel.
*/
    fun usedByNode(cself: Channel, nodeName: String): Boolean {
        for(mb in cself.getBindings()){
            val pointednode = mb.getPort()!!.eContainer()!!.eContainer() as ContainerNode
            if(pointednode.getName() == nodeName){
                return true
            }
        }
        return false
    }

    /**
* Returns a list of nodes the channel is linked with, except the node given in parameter.
*/
    fun getConnectedNode(cself: Channel, nodeName: String): List<ContainerNode> {
        var result = ArrayList<ContainerNode>()
        for(mb in cself.getBindings()){
            val pointednode = mb.getPort()!!.eContainer()!!.eContainer() as ContainerNode
            if(pointednode.getName() != nodeName){
                result.add(pointednode)
            }
        }
        return result
    }

    /**
   * Returns the list of all ContainerNode this binding is connected to.
   */
    fun getRelatedNodes(cself: Channel): List<ContainerNode> {
        var result = ArrayList<ContainerNode>()
        for(mb in cself.getBindings()){
            if(!result.contains(mb.getPort()!!.eContainer()!!.eContainer() as ContainerNode)){
                result.add(mb.getPort()!!.eContainer()!!.eContainer() as ContainerNode)
            }
        }
        return result
    }

    /**
  * Returns the list of bindings belonging to this channel on the given node
  */
    fun getRelatedBindings(cself: Channel, node: ContainerNode): List<MBinding> {
        var result = ArrayList<MBinding>()
        for(mb in cself.getBindings()){
            if ((mb.getPort()!!.eContainer()!!.eContainer() as  ContainerNode).getName() == node.getName()) {
                result.add(mb)
            }
        }
        return result
    }

}