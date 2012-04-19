package org.kevoree.library.voldemortChannels

import actors.DaemonActor
import voldemort.store.bdb.BdbStorageConfiguration
import voldemort.server.{VoldemortServer, VoldemortConfig}
import voldemort.cluster.{Node, Cluster}
import voldemort.utils.Props
import org.kevoree.extra.voldemort.KUtils
import java.io.File
import org.apache.commons.io.FileUtils

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 09:59
 */

class KServer(config: VoldemortConfig, nameCluster: String, nodes: java.util.List[Node]) extends  DaemonActor {

  private var cluster: Cluster = null
  private var bdbStorage: BdbStorageConfiguration = null
  private var standalone: Thread = null
  private var server: VoldemortServer = null

  def act() {
    cluster = new Cluster(nameCluster,nodes)
    server = new VoldemortServer(config, cluster)
    server.start()
  }


  def stop(){
    if(server != null){
      try {
        server.stop()
      }  catch {
        case _ => // ignore
      }
    }

  }

}