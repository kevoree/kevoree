package org.kevoree.library.sky.virtualCloud

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 00:01
 * To change this template use File | Settings | File Templates.
 */

object VirtualNodesHandler {

  case class VirtualNodeInformation(nodeName : String,outFilePath : String,errFilePath : String, process : Process)

  private var cache : List[VirtualNodeInformation] = List()

  def addNodeInfo(nodeName : String,outFilePath : String,errFilePath : String, process : Process){
     cache = cache ++ List(VirtualNodeInformation(nodeName,outFilePath,errFilePath,process))
  }

  def remove(nodeName : String){
      cache = cache.filterNot(c => c.nodeName == nodeName)
  }

  def exportNodeListAsHTML : String = {
    (<html>

    </html>).toString()
  }


}