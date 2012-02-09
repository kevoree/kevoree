/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/02/12
 * Time: 12:53
 */

package org.jboss.netty.handler.codec.serialization {

import java.io.InputStream
import java.lang.Class
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import org.jboss.netty.buffer.{ChannelBufferInputStream, ChannelBuffer}
import org.kevoree.library.nioChannel.NioChannel

class KevoreeBindingObjectDecoder(nioChannel: NioChannel) extends LengthFieldBasedFrameDecoder(2548576, 0, 4, 0, 4) {
  override def decode(ctx: ChannelHandlerContext, channel: Channel, buffer: ChannelBuffer): AnyRef = {
    val frame = super.decode(ctx, channel, buffer).asInstanceOf[ChannelBuffer]
    if (frame == null) {
      return null;
    }
    new KevoreeCompactObjectInputStream(new ChannelBufferInputStream(frame)).readObject();
  }

  class KevoreeCompactObjectInputStream(st: InputStream) extends CompactObjectInputStream(st) {
    override def loadClass(className: String): Class[_] = {
      import scala.collection.JavaConversions._
      val model = nioChannel.getModelService.getLastModel
      val currentNode = model.getNodes.find(n => n.getName == nioChannel.getNodeName).get
      nioChannel.getBindedPorts.foreach{ bport =>
        currentNode.getComponents.find(c => c.getName == bport.getName) match {
          case Some(component)=> {
            import org.kevoree.framework.aspects.KevoreeAspects._
            val du = component.getTypeDefinition.foundRelevantDeployUnit(currentNode)
            if(du != null){
              val kcl = nioChannel.getBootStrapperService.getKevoreeClassLoaderHandler.getKevoreeClassLoader(du)
              try {
                val loadedClass = kcl.loadClass(className)
                return loadedClass
              } catch {
                case _ => //IGNORE
              }
            }
          }
          case _ =>
        }
      }
      null
    }
  }

}


}

