package org.kevoree.framework.kaspects

import org.kevoree.TypeDefinition
import java.util.HashSet
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import java.util.ArrayList

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


}