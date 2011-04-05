package org.kevoree.library.gossiperNetty

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 14:31
 */

trait Serializer {

	def serialize(data : Any) : Array[Byte]
	def deserialize(data : Array[Byte]) : Any
}