package org.kevoree.framework.kaspects

import java.util.ArrayList
import java.util.HashSet
import org.kevoree.Channel
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.Group
import org.kevoree.TypeDefinition

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/03/13
 * Time: 09:49
 */
class ContainerNodeAspect {

    private fun getTypeAndInherited(t: TypeDefinition): List<TypeDefinition> {
        var types = ArrayList<TypeDefinition>()
        if (t.getSuperTypes() != null) {
            for(superT in t.getSuperTypes()){
                types.addAll(getTypeAndInherited(superT))
            }
            types.add(t)
        }
        return types
    }

    fun getUsedTypeDefinition(node: ContainerNode): List<TypeDefinition> {
        var usedType: HashSet<TypeDefinition> = HashSet()
        /* ADD NODE TYPE DEFINITION */
        usedType.addAll(getTypeAndInherited(node.getTypeDefinition()))
        /* ADD COMPONENT TYPE USED */
        for(c in node.getComponents()){
            usedType.addAll(getTypeAndInherited(c.getTypeDefinition()))
        }

        /* ADD CHANNEL TYPE USED */
        /* add channel fragment on node */
        val rootModel = node.eContainer() as ContainerRoot
        for(mb in rootModel.getMBindings()) {
            if (mb.getPort()!!.eContainer()!!.eContainer() == node) {
                usedType.addAll(getTypeAndInherited(mb.getHub()!!.getTypeDefinition()))
            }
        }

        /* add group type on node */
        /* add group */
        for(group in rootModel.getGroups()){
            if(group.getSubNodes().contains(node)){
                usedType.addAll(group.getTypeDefinition())
            }
        }
        return usedType.toList()
    }

    fun getChannelFragment(node: ContainerNode): List<Channel> {
        /* add channel fragment on node */
        val usedChannels = ArrayList<Channel>()
        for (component in node.getComponents()) {
            for (port in component.getProvided()) {
                for (mb in port.getBindings()) {
                    if (!usedChannels.contains(mb.getHub())) {
                        usedChannels.add(mb.getHub()!!)
                    }
                }
            }
            for (port in component.getRequired()) {
                for (mb in port.getBindings()) {
                    if (!usedChannels.contains(mb.getHub())) {
                        usedChannels.add(mb.getHub()!!)
                    }
                }
            }
        }
        return usedChannels
    }

    fun getGroups(node: ContainerNode): List<Group> {
        val usedGroups = ArrayList<Group>()
        for (group in (node.eContainer() as ContainerRoot).getGroups()) {
            if (group.getSubNodes().contains(node)) {
                usedGroups.add(group)
            }
        }
        return usedGroups
    }

}