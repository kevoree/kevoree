/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.experiment.modelScript


case class NodePacket(name : String,ip : String, firstPort : Int,nbElem:Int)

object TopologyGeneratorScript {

  def generate(packets : List[NodePacket], logSrvIP:String) : String = {
    //STEP GENERATE NODE
    val tscript = new StringBuilder
    val groupPort = new StringBuilder
    
    //GENERATE PACKET
    packets.foreach{packet =>      
      for (i <- 0 until packet.nbElem) {
        //GENRATE NODE NAME
        tscript append generateNodeScript(packet.name+i)
        //GENERATE PACKET INTERNAL LINK
        for (i2 <- 0 until packet.nbElem) {        
          //AVOID LOOP
          if(i != i2){
            val remotePort : Int = packet.firstPort + i2
            val remoteName : String = packet.name + i2
            
            val localPort : Int = packet.firstPort + i
            val localName : String = packet.name + i

            tscript append generateLink( localName , remoteName , packet.ip, remotePort)
            tscript append generateLink( remoteName , localName , packet.ip, localPort)
          }
        }
        //GENERATE GROUP PORT
        for (i <- 0 until packet.nbElem) {
          if (!groupPort.isEmpty) {
            groupPort.append(",")
          }
          groupPort.append(packet.name)
          groupPort.append(i)
          groupPort.append("=")
          groupPort.append(packet.firstPort + i)
        }
      }
      //GENERATE PACKET LINK
      //TODO

    }
    
    //GENERATE GLOBAL GROUP INSTANCE
    //ADD GLOBAL GROUP
    tscript append "\n"
    tscript append "addGroup gossipGroup : LogNettyGossiperGroup {"
    tscript append "port=\"" + groupPort.toString + "\"\n"
    tscript append ",loggerServerIP=\"" + logSrvIP + "\""
    tscript append "}\n"
    //BIND ALL NODE TO GROUP
    tscript append "addToGroup gossipGroup * \n"
    
    tscript.toString()
  }

  def generateNodeScript(stringNodeName:String) : String = {
    val tscript = new StringBuilder
    tscript append "\n"
    tscript append "addNode " + stringNodeName
    tscript append " : "
    tscript append "JavaSENode"
    tscript.append("\n")
    tscript.toString()
  }
  
  def generateLink(srcName: String, targetName: String,ip:String,port:Int): String = {
    val tscript = new StringBuilder
    tscript append "network "
    tscript append srcName
    tscript append " => "
    tscript append targetName
    tscript append " { \"KEVOREE.remote.node.modelsynch.port\"= \""
    tscript append port
    tscript append "\"}\n"

    tscript append "network "
    tscript append srcName
    tscript append " => "
    tscript append targetName
    tscript append " { \"KEVOREE.remote.node.ip\"= \""
    tscript append ip
    tscript append "\"}\n"
    tscript.toString()
  } 
}
