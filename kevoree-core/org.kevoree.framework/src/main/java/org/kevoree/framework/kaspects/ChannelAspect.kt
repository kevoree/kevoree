package org.kevoree.framework.kaspects

import org.kevoree.Channel
import org.kevoree.ContainerNode
import java.util.ArrayList

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


}