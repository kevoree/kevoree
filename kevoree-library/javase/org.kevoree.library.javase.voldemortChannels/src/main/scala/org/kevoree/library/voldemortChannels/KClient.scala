package org.kevoree.library.voldemortChannels

import voldemort.cluster.Node
import actors.DaemonActor
import collection.mutable.HashSet
import java.util.ArrayList
import voldemort.client.{ClientConfig, SocketStoreClientFactory, StoreClient, StoreClientFactory}
import voldemort.VoldemortException
import voldemort.cluster.failuredetector.FailureDetectorListener

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 11:13
 */

class KClient(nodes: java.util.List[Node])   {

  val flb = new RoundRobinLoadBalancerFactory()
  var current: StoreClientFactory = null

  var FailureDetectorListener = new FailureDetectorListener{
    def nodeUnavailable(p1: Node) {
      println("nodeUnavailable "+p1)
      current = null
    }

    def nodeAvailable(p1: Node) {
      println("nodeAvailable "+p1)

    }
  }

  def lb()
  {
    if(current == null){
      import scala.collection.JavaConversions._
      val lb : LoadBalancer =   flb.newLoadBalancer(nodes.toSet)
      val node =    lb.nextNode.get
      node match  {
        case classOf: Node => {
          try
          {
            current = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls("tcp://" + node.getHost + ":" + node.getSocketPort))
            current.getFailureDetector.addFailureDetectorListener(FailureDetectorListener)
          } catch
            {
              case e : Exception => {
                current.close()
                current = null
              }
            }
        }
        case _ =>    {
          current = null
        }
      }
    }
  }
  def getStore(storeName : String) : StoreClient[_, _] = {
    do
    {
      lb()
    } while(current == null)
    current.getStoreClient(storeName)
  }
  
  



}