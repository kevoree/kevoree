/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework.aspects


import org.kevoree._
 import KevoreeAspects._

case class PortAspect(p : Port) {

  def removeAndUnbind()={
    //REMOVE ALL BINDING BINDED TO
    val root = p.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerRoot]
    val mbindings = root.getMBindings.filter(b=>b.getPort == p) ++ List()
    mbindings.foreach{mb=> root.removeMBindings(mb)}

    //REMOVE PORT
    if(p.isProvidedPort){
      if(p.eContainer.asInstanceOf[ComponentInstance].getProvided.contains(p)){p.eContainer.asInstanceOf[ComponentInstance].removeProvided(p)}
    } else {
    if(p.isRequiredPort){
      if(p.eContainer.asInstanceOf[ComponentInstance].getRequired.contains(p)){p.eContainer.asInstanceOf[ComponentInstance].removeRequired(p)}
    }}
    
    

  }

  def isProvidedPort : Boolean = {
    p.eContainer.asInstanceOf[ComponentInstance].getProvided.contains(p)
  }

  def isRequiredPort : Boolean = {
    p.eContainer.asInstanceOf[ComponentInstance].getRequired.contains(p)
  }


  def isModelEquals(pp : Port) : Boolean={
    (p.getPortTypeRef.getName == pp.getPortTypeRef.getName) && 
    (p.eContainer.asInstanceOf[ComponentInstance].isModelEquals(pp.eContainer.asInstanceOf[ComponentInstance]))
  }

  def isBind : Boolean ={
    val container = p.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerRoot]
    var mb = p.getPortTypeRef.getRef
    container.getMBindings.exists({mb => mb.getPort == p})
    /*
     p.getPortTypeRef.getRef match {
     case mpt : MessagePortType => container.getMBindings.exists({mb => mb.getPort == p})
     case spt : ServicePortType => container.getBindings.exists({b => b.isUsingPort(p)})
     }*/
  }

  def getProxyURI : String = {
    val container : ContainerRoot = p.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerRoot]
    if(p.isBind){
      p.getPortTypeRef.getRef match {
        case spt : ServicePortType => p.eContainer.asInstanceOf[ComponentInstance].getName.toString+"."+p.getPortTypeRef.getName
        case mpt : MessagePortType => container.getMBindings.find({mb=> mb.getPort == p}).get.getHub.getName
        case _ => println("Art2 Deploy Error, , getProxyURI");""
      }
    } else {
      ""
    }
  }

  /*
   def getProxyHubType() : String = {
   var container : ContainerRoot = p.eContainer.eContainer.eContainer.asInstanceOf[ContainerRoot]
   if(p.isBind){
   p.getPortTypeRef.getRef match {
   case spt : ServicePortType => "queue"
   case mpt : MessagePortType => container.getMBindings.find({mb=> mb.getPort == p}).get.getHub match {
   case t : Topic => "topic"
   case q : Queue => "queue"
   case mh : MessageHub => "topic"
   case _ => ""
   }
   case _ => println("Art2 Deploy Error, , getProxyURI");""
   }

   } else {
   ""
   }
   }*/

}

