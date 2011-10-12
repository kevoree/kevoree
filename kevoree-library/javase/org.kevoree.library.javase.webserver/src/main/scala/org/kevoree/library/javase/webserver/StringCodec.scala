package org.kevoree.library.javase.webserver

import com.twitter.finagle.{Codec}
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}
import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.util.CharsetUtil


object StringCodec extends StringCodec

class StringCodec extends Codec[String, String] {

  def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = {
        val pipeline = Channels.pipeline()
        pipeline.addLast("line",
          new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter: _*))
        pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8))
        pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8))
        pipeline
      }
    }
}