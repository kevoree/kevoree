package org.kevoree.library.voldemortChannels

import voldemort.client.StoreClientFactory
import voldemort.cluster.Node

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 09:50
 */

trait LoadBalancer {
  def nextNode: Option[Node]
}

trait LoadBalancerFactory {
  @throws(classOf[Exception])
  def newLoadBalancer(nodes: Set[Node]): LoadBalancer
}

