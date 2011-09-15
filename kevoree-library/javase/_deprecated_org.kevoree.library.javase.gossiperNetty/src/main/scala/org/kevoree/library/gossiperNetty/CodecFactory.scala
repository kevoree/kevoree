package org.kevoree.library.gossiperNetty

import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import com.twitter.finagle.Codec
import org.jboss.netty.channel.{ChannelPipelineFactory, Channels}
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf._

object CodecFactory extends Codec[Message, Message] {

  def getUDPCodec : Codec ={
    new UDPCodec
  }

  def getTCPCodec : Codec ={
    new TCPCodec
  }


  class UDPCodec extends Codec[Message, Message] {

    //override def serverCodec = new ServerCodec[Message, Message] {
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

    //override def serverCodec = new ServerCodec[Message, Message] {
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


  /*override def clientCodec = new ClientCodec[Message, Message] {
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
          }*/
}