package org.kevoree.framework.message

import java.io.Serializable
import java.util.UUID

class Message: Serializable {

    var destNodeName = "default"

    fun getDestNodeName(): String {
        return destNodeName
    }

    fun setDestNodeName(newDestNodeName: String) {
        destNodeName = newDestNodeName
    }

    var destChannelName = "default"

    fun getDestChannelName(): String {
        return destChannelName
    }

    fun setDestChannelName (newDestChannelName: String) {
        destChannelName = newDestChannelName
    }

    var content: Any? = null

    fun getContent(): Any? {
        return content
    }

    fun setContent (newContent: Any) {
        content = newContent
    }

    var contentClass: String? = null

    fun getContentClass(): String? {
        return contentClass
    }

    fun setContentClass (newContentClass: String) {
        contentClass = newContentClass
    }

    var inOut: Boolean = false

    fun getInOut(): Boolean {
        return inOut
    }

    fun setInOut (newInOut: Boolean) {
        inOut = newInOut
    }

    var responseTag = ""

    fun getResponseTag(): String {
        return responseTag
    }

    fun setResponseTag (newResponseTag: String) {
        responseTag = newResponseTag
    }

    var timeout: Long = 3000

    fun getTimeout(): Long {
        return timeout
    }

    fun setTimeout (newTimeout: Long) {
        timeout = newTimeout
    }

    var passedNodes: List<String> = java.util.ArrayList<String>()

    fun getPassedNodes(): List<String> {
        return passedNodes
    }

    fun setPassedNodes (newPassedNodes: List<String>) {
        passedNodes = newPassedNodes
    }

    var uuid: UUID = UUID.randomUUID()

    fun getUuid(): UUID {
        return uuid
    }

    fun setUuid (newUuid: UUID) {
        uuid = newUuid
    }

    override fun clone(): Message {
        val clone = Message()
        clone.setDestNodeName(this.getDestNodeName())
        clone.setDestChannelName(this.getDestChannelName())
        clone.setContent(this.getContent())
        clone.setContentClass(this.getContentClass())
        clone.setInOut(this.getInOut())
        clone.setResponseTag(this.getResponseTag())
        clone.setTimeout(this.getTimeout())
        clone.setPassedNodes(this.getPassedNodes())
        clone.setUuid(this.getUuid())
        return clone
    }


}

