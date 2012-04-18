package org.kevoree.library.voldemortChannels

import voldemort.client.StoreClientFactory
import voldemort.cluster.Node

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 09:49
 */

class RoundRobinLoadBalancerFactory extends  LoadBalancerFactory {
  def newLoadBalancer(nodes: Set[Node]) = new LoadBalancer {
      private val random = new scala.util.Random
      private val myNodes = nodes.toArray
      def nextNode = Some(myNodes(random.nextInt(myNodes.length)))
    }

}