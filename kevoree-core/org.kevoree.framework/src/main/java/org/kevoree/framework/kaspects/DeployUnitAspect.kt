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
        return self.getUnitName() + "." + self.getGroupName() + "." + self.getVersion()
    }

    fun isModelEquals(self: DeployUnit, other: DeployUnit): Boolean {
        if(other == null){
            if(self == null){
                return true
            } else {
                return false
            }
        }
        if (other.getUnitName() != self.getUnitName() || other.getGroupName() != self.getGroupName() || other.getVersion() != self.getVersion()) {
            return false
        }
        if (other.getTargetNodeType() != null && self.getTargetNodeType() == null) {
            return false
        }

        if (other.getTargetNodeType() == null && self.getTargetNodeType() != null) {
            return false
        }
        if (other.getTargetNodeType() != null && self.getTargetNodeType() != null) {
            return other.getTargetNodeType()!!.getName() == self.getTargetNodeType()!!.getName()
        } else {
            return true
        }
    }

    fun isUpdated(self: DeployUnit, targetDU: DeployUnit, alreadyCheck: java.util.HashMap<String, Boolean>): Boolean {
        if (alreadyCheck.containsKey(buildKey(self))) {
            return alreadyCheck.get(buildKey(self))!!
        } else {
            try {
                if (targetDU.getHashcode() == "" && self.getHashcode() == "") {
                    alreadyCheck.put(buildKey(self), false)
                    return false
                } else {
                    val pDUInteger = java.lang.Long.parseLong(targetDU.getHashcode())
                    val selfDUInteger = java.lang.Long.parseLong(self.getHashcode())
                    alreadyCheck.put(buildKey(self), (selfDUInteger != pDUInteger))
                    return ((selfDUInteger != pDUInteger) || checkTransitiveUpdate(self,targetDU, alreadyCheck))
                }
            } catch(e: Exception) {
                return  targetDU.getHashcode() != self.getHashcode()
            }
        }
    }

    fun checkTransitiveUpdate(self: DeployUnit, targetDU: DeployUnit, alreadyCheck: java.util.HashMap<String, Boolean>): Boolean {
        if (self.getRequiredLibs().size != targetDU.getRequiredLibs().size) {
            return true
        } else {
            for(selfRDU in self.getRequiredLibs()){
                var updateFound = false
                @internalL for(tDU in targetDU.getRequiredLibs()){
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
    }


}

