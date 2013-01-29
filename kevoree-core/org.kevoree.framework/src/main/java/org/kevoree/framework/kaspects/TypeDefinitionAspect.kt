package org.kevoree.framework.kaspects

import org.kevoree.TypeDefinition
import org.kevoree.DeployUnit

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
            val superUpdate = pTD.getSuperTypes().find{ ostd -> std.getName() == ostd.getName() }
            if(superUpdate != null){
                isUpdated(superUpdate, pTD)
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

}
