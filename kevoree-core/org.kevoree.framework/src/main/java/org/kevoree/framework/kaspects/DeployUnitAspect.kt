package org.kevoree.framework.kaspects

import org.kevoree.DeployUnit

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/29/13
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */

class DeployUnitAspect {

    fun buildKey(self: DeployUnit): String {
        return self.name + "." + self.groupName + "." + self.version
    }

    /*
    fun isModelEquals(self: DeployUnit, other: DeployUnit): Boolean {
        if(other == null){
            if(self == null){
                return true
            } else {
                return false
            }
        }
        if (other.name != self.name || other.groupName != self.groupName || other.version != self.version) {
            return false
        }
        if (other.targetNodeType != null && self.targetNodeType != null) {
            return other.targetNodeType!!.name == self.targetNodeType!!.name
        } else {
            return true
        }
    }  */

    fun isUpdated(self: DeployUnit, targetDU: DeployUnit, alreadyCheck: java.util.HashMap<String, Boolean>): Boolean {
        if (alreadyCheck.containsKey(buildKey(self))) {
            return alreadyCheck.get(buildKey(self))!!
        } else {
            try {
                if (targetDU.hashcode == "" && self.hashcode == "") {
                    alreadyCheck.put(buildKey(self), false)
                    return false
                } else {
                    val pDUInteger = java.lang.Long.parseLong(targetDU.hashcode!!)
                    val selfDUInteger = java.lang.Long.parseLong(self.hashcode!!)
                    alreadyCheck.put(buildKey(self), (selfDUInteger != pDUInteger))
                    return ((selfDUInteger != pDUInteger) /*|| checkTransitiveUpdate(self,targetDU, alreadyCheck)*/)
                }
            } catch(e: Exception) {
                return  targetDU.hashcode != self.hashcode
            }
        }
    }

    /*
    fun checkTransitiveUpdate(self: DeployUnit, targetDU: DeployUnit, alreadyCheck: java.util.HashMap<String, Boolean>): Boolean {
        if (self.requiredLibs.size != targetDU.requiredLibs.size) {
            return true
        } else {
            for(selfRDU in self.requiredLibs){
                var updateFound = false
                @internalL for(tDU in targetDU.requiredLibs){
                    if(isModelEquals(selfRDU, tDU)){
                        if(isUpdated(selfRDU, tDU, alreadyCheck)){
                            updateFound=true
                            break@internalL
                        }
                    }
                }
                if(updateFound){
                    return true
                }
            }
            return false
        }
    } */


}

