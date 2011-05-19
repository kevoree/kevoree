package org.kevoree.experiment.modelScript

import java.io.File
import java.io.FileWriter
import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 19/05/11
 * Time: 13:09
 */

object Kev2GraphML {

  implicit def conv(model:ContainerRoot) : String = toGraphML(model)
  def toGraphML(model: ContainerRoot) : String = {
    val content = 
      <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
        <graph edgedefault="directed">   
          <!-- GENERATE NODES NAME -->
          {
            var nodes : scala.collection.mutable.ArrayBuffer[scala.xml.Elem] = new scala.collection.mutable.ArrayBuffer[scala.xml.Elem]
            model.getNodes.foreach{ node =>
              nodes.add(<node id= {node.getName}/>)
            }
            nodes
          } 
          <!-- GENERATE EDGES NAME -->
          {
            var edges : scala.collection.mutable.ArrayBuffer[scala.xml.Elem] = new scala.collection.mutable.ArrayBuffer[scala.xml.Elem]
            model.getNodeNetworks.foreach{ nodeNetwork =>
              edges.add(<edge id={nodeNetwork.hashCode+""} source={nodeNetwork.getInitBy.getName} target={nodeNetwork.getTarget.getName}/>)
            }
            edges
          }
        </graph>
      </graphml>
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + content.toString
  }
  
  def toGraphMLFile(stringName : String,model: ContainerRoot)={
    val file = new File(stringName+".graphml")
    val fw = new FileWriter(file)
    fw.write(model)
    fw.close
  }
  
  


}