package org.kevoree.framework.message

import java.io.Serializable
import java.util.UUID

class Message: Serializable {

    var _destNodeName = "default"

    fun getDestNodeName(): String {
        return _destNodeName
    }

    fun setDestNodeName(newDestNodeName: String) {
        _destNodeName = newDestNodeName
    }

    var _destChannelName = "default"

    fun getDestChannelName(): String {
        return _destChannelName
    }

    fun setDestChannelName (newDestChannelName: String) {
        _destChannelName = newDestChannelName
    }

    var _content: Any? = null

    fun getContent(): Any? {
        return _content
    }

    fun setContent (newContent: Any) {
        _content = newContent
    }

    var _contentClass: String? = null

    fun getContentClass(): String? {
        return _contentClass
    }

    fun setContentClass (newContentClass: String) {
        _contentClass = newContentClass
    }

    var _inOut: Boolean = false

    fun getInOut(): Boolean {
        return _inOut
    }

    fun setInOut (newInOut: Boolean) {
        _inOut = newInOut
    }

    var _responseTag = ""

    fun getResponseTag(): String {
        return _responseTag
    }

    fun setResponseTag (newResponseTag: String) {
        _responseTag = newResponseTag
    }

    var _timeout: Long = 3000

    fun getTimeout(): Long {
        return _timeout
    }

    fun setTimeout (newTimeout: Long) {
        _timeout = newTimeout
    }

    var _passedNodes: List<String> = java.util.ArrayList<String>()

    fun getPassedNodes(): List<String> {
        return _passedNodes
    }

    fun setPassedNodes (newPassedNodes: List<String>) {
        _passedNodes = newPassedNodes
    }

    var _uuid: UUID = UUID.randomUUID()

    fun getUuid(): UUID {
        return _uuid
    }

    fun setUuid (newUuid: UUID) {
        _uuid = newUuid
    }

    override public fun clone(): Message {
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

