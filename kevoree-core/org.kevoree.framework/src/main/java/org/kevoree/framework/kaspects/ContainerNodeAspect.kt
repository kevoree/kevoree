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

    private fun getTypeAndInherited(t: TypeDefinition?): List<TypeDefinition> {
        var types = ArrayList<TypeDefinition>()
        if (t != null && t.superTypes != null) {
            for(superT in t.superTypes){
                types.addAll(getTypeAndInherited(superT))
            }
            types.add(t)
        }
        return types
    }

    fun getUsedTypeDefinition(node: ContainerNode): List<TypeDefinition> {
        var usedType: HashSet<TypeDefinition> = HashSet()
        /* ADD NODE TYPE DEFINITION */
        usedType.addAll(getTypeAndInherited(node.typeDefinition))
        /* ADD COMPONENT TYPE USED */
        for(c in node.components){
            usedType.addAll(getTypeAndInherited(c.typeDefinition))
        }

        /* ADD CHANNEL TYPE USED */
        /* add channel fragment on node */
        val rootModel = node.eContainer() as ContainerRoot
        for(mb in rootModel.mBindings) {
            if (mb.port!!.eContainer()!!.eContainer() == node) {
                usedType.addAll(getTypeAndInherited(mb.hub!!.typeDefinition))
            }
        }

        /* add group type on node */
        /* add group */
        for(group in rootModel.groups){
            if(group.subNodes.contains(node)){
                val groupTypeDef = group.typeDefinition
                if(groupTypeDef != null){
                    usedType.add(groupTypeDef)
                }
            }
        }
        return usedType.toList()
    }

    fun getChannelFragment(node: ContainerNode): List<Channel> {
        /* add channel fragment on node */
        val usedChannels = ArrayList<Channel>()
        for (component in node.components) {
            for (port in component.provided) {
                for (mb in port.bindings) {
                    if (!usedChannels.contains(mb.hub)) {
                        usedChannels.add(mb.hub!!)
                    }
                }
            }
            for (port in component.required) {
                for (mb in port.bindings) {
                    if (!usedChannels.contains(mb.hub)) {
                        usedChannels.add(mb.hub!!)
                    }
                }
            }
        }
        return usedChannels
    }

    fun getGroups(node: ContainerNode): List<Group> {
        val usedGroups = ArrayList<Group>()
        for (group in (node.eContainer() as ContainerRoot).groups) {
            if (group.subNodes.contains(node)) {
                usedGroups.add(group)
            }
        }
        return usedGroups
    }

}