package org.kevoree.platform.agent

import com.twitter.finagle.{Codec, ClientCodec, ServerCodec}
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}
import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.util.CharsetUtil
/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 06/05/11
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */

object StringCodec extends StringCodec

class StringCodec extends Codec[String, String] {
  override def serverCodec = new ServerCodec[String, String] {
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

  override def clientCodec = new ClientCodec[String, String] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = {
        val pipeline = Channels.pipeline()
        pipeline.addLast("stringEncode", new StringEncoder(CharsetUtil.UTF_8))
        pipeline.addLast("stringDecode", new StringDecoder(CharsetUtil.UTF_8))
        pipeline
      }
    }
  }
}