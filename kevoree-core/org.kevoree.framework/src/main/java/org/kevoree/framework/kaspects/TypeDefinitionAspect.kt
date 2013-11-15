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

    /*
    fun isUpdated(selfTD: TypeDefinition, pTD: TypeDefinition): Boolean {
        if (pTD.deployUnits.size == 0 && selfTD.deployUnits.size > 0) {
            return false
        } //SPECIAL CASE DONT MERGE TYPE DEFINITION WITHOUT DEPLOY UNIT
        if (selfTD.deployUnits.size != pTD.deployUnits.size) {
            return true
        }

        val updated = selfTD.superTypes.any{ std ->
            val superUpdate = pTD.findSuperTypesByID(std.name!!)
            if(superUpdate != null){
                isUpdated(superUpdate, std)
            } else {
                true
            }
        }

        //EQUALS DEPLOY UNIT SIZE CHECK FOR ONE IS UPDATED
        val oneUpdated = selfTD.deployUnits.any{ selfDU ->
            val pDU = pTD.deployUnits.find{ p -> duA.isModelEquals(p, selfDU) }
            if(pDU != null){
                duA.isUpdated(selfDU, pDU, java.util.HashMap<String, Boolean>())
            } else {
                true
            }
        }
        return oneUpdated || updated
    }  */

    fun foundRelevantDeployUnit(selfTD: TypeDefinition): DeployUnit? {
        var deployUnitfound: DeployUnit? = selfTD.deployUnit
        /*if (deployUnitfound == null) {
            deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(node.typeDefinition!! as NodeType, selfTD)
        }*/
        return deployUnitfound
    }

    /*
    private fun foundRelevantDeployUnitOnNodeSuperTypes(nodeType: NodeType, t: TypeDefinition): DeployUnit? {
        var deployUnitfound: DeployUnit? = null
        // looking for relevant deployunits on super types
        for(td in t.deployUnits) {
            if (td.targetNodeType != null) {
                if (td.targetNodeType!!.name == nodeType.name) {
                    deployUnitfound = td
                    return deployUnitfound
                }
            }
        }
        if (deployUnitfound == null) {
            for(superNode in  nodeType.superTypes) {
                deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(superNode as NodeType, t)
                if (deployUnitfound != null) {
                    return deployUnitfound
                }
            }
        }
        return deployUnitfound
    }       */
     /*
    fun foundRelevantHostNodeType(nodeType: NodeType, targetTypeDef: TypeDefinition): NodeType? {
        for (deployUnit in targetTypeDef.deployUnits) {
            if (deployUnit.targetNodeType != null && deployUnit.targetNodeType == nodeType) {
                return nodeType
            }
        }
        for (superType in nodeType.superTypes) {
            val returnType = foundRelevantHostNodeType(superType as NodeType, targetTypeDef)
            if (returnType != null) {
                return returnType
            }
        }
        return null
    } */
}
