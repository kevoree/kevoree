package org.kevoree.framework.kaspects

import org.kevoree.ContainerNode
import org.kevoree.DeployUnit
import org.kevoree.NodeType
import org.kevoree.TypeDefinition

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/29/13
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */

class TypeDefinitionAspect {

    val duA = DeployUnitAspect()

    fun isUpdated(selfTD: TypeDefinition, pTD: TypeDefinition): Boolean {
        if (pTD.getDeployUnits().size == 0 && selfTD.getDeployUnits().size > 0) {
            return false
        } //SPECIAL CASE DONT MERGE TYPE DEFINITION WITHOUT DEPLOY UNIT
        if (selfTD.getDeployUnits().size != pTD.getDeployUnits().size) {
            return true
        }

        val updated = selfTD.getSuperTypes().any{ std ->
            val superUpdate = pTD.findSuperTypesByID(std.getName())
            if(superUpdate != null){
                isUpdated(superUpdate, std)
            } else {
                true
            }
        }

        //EQUALS DEPLOY UNIT SIZE CHECK FOR ONE IS UPDATED
        val oneUpdated = selfTD.getDeployUnits().any{ selfDU ->
            val pDU = pTD.getDeployUnits().find{ p -> duA.isModelEquals(p, selfDU) }
            if(pDU != null){
                duA.isUpdated(selfDU, pDU, java.util.HashMap<String, Boolean>())
            } else {
                true
            }
        }
        return oneUpdated || updated
    }

    fun foundRelevantDeployUnit(selfTD: TypeDefinition, node: ContainerNode): DeployUnit? {
        var deployUnitfound: DeployUnit? = selfTD.getDeployUnits().find{ du -> du.getTargetNodeType() != null && du.getTargetNodeType()!!.getName() == node.getTypeDefinition()!!.getName() }
        if (deployUnitfound == null) {
            deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(node.getTypeDefinition() as NodeType, selfTD)
        }
        return deployUnitfound
    }

    private fun foundRelevantDeployUnitOnNodeSuperTypes(nodeType: NodeType, t: TypeDefinition): DeployUnit? {
        var deployUnitfound: DeployUnit? = null
        // looking for relevant deployunits on super types
        for(td in t.getDeployUnits()) {
            if (td.getTargetNodeType() != null) {
                if (td.getTargetNodeType()!!.getName() == nodeType.getName()) {
                    deployUnitfound = td
                    return deployUnitfound
                }
            }
        }
        if (deployUnitfound == null) {
            for(superNode in  nodeType.getSuperTypes()) {
                deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(superNode as NodeType, t)
                if (deployUnitfound != null) {
                    return deployUnitfound
                }
            }
        }
        return deployUnitfound
    }

    fun foundRelevantHostNodeType(nodeType: NodeType, targetTypeDef: TypeDefinition): NodeType? {
        for (deployUnit in targetTypeDef.getDeployUnits()) {
            if (deployUnit.getTargetNodeType() != null && deployUnit.getTargetNodeType() == nodeType) {
                return nodeType
            }
        }
        for (superType in nodeType.getSuperTypes()) {
            val returnType = foundRelevantHostNodeType(superType as NodeType, targetTypeDef)
            if (returnType != null) {
                return returnType
            }
        }
        return null
    }
}
