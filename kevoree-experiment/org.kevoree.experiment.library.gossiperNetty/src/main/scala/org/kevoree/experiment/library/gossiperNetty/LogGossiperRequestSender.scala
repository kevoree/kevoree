package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.library.gossiperNetty.GossiperRequestSender
import java.net.SocketAddress

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/05/11
 * Time: 14:14
 */

class LogGossiperRequestSender extends GossiperRequestSender {

  @Override protected def writeMessage (o: Object, address: SocketAddress) {
    FailureSimulation.failureOutNode
    channel.write(o, address)
  }
}