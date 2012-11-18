package org.kevoree.library.javase.basicGossiper


trait Serializer {

	def serialize(data : Any) : Array[Byte]
	def deserialize(data : Array[Byte]) : Any
}