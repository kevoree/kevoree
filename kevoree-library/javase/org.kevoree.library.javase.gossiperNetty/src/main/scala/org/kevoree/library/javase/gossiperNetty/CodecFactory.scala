/*
package org.kevoree.library.javase.gossiperNetty

import com.twitter.finagle.Codec
import org.jboss.netty.channel.{ChannelPipelineFactory, Channels}
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf._
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message

object CodecFactory {

  def getUDPCodec : Codec[Message, Message] ={
    new UDPCodec
  }

  def getTCPCodec : Codec[Message, Message] ={
    new TCPCodec
  }


  class UDPCodec extends Codec[Message, Message] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = {
        val p = Channels.pipeline()
//        p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
//        p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
        p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
        p.addLast("protobufEncoder", new ProtobufEncoder)
        p
      }
    }
  }

  class TCPCodec extends Codec[Message, Message] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = {
        val p = Channels.pipeline()
        p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
        p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
        p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
        p.addLast("protobufEncoder", new ProtobufEncoder)
        p
      }
    }
  }
}*/
