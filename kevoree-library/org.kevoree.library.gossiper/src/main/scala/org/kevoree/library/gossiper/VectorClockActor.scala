/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock
import scala.collection.JavaConversions._

class VectorClockActor(selfNodeName : String) extends actors.DaemonActor {
  
  private var logger = org.slf4j.LoggerFactory.getLogger(classOf[VectorClockActor])
  
  this.start
  
  case class STOP_GOSSIPER()
  case class GET()
  case class SET(newclock : VectorClock)
  case class INC_GET()
  case class MERGE(newclock : VectorClock)
  
  def stop() ={this ! STOP_GOSSIPER()}
  def get():VectorClock = {(this !? GET()).asInstanceOf[VectorClock] }
  def incAndGet():VectorClock = {(this !? INC_GET()).asInstanceOf[VectorClock] }
  def swap(v : VectorClock) : VectorClock  = {(this !? SET(v)).asInstanceOf[VectorClock] }
  def merge(v : VectorClock) = {(this !? MERGE(v)).asInstanceOf[VectorClock] }

  private var current : VectorClock = VectorClock.newBuilder.setTimestamp(System.currentTimeMillis()).build
  
  def act(){
    loop {
      react{
        case GET() => { sender ! current }
        case SET(newClock) => { current = newClock ; sender ! current }
        case INC_GET()=> { current = increment() ; sender ! current }
        case MERGE(newClock)=> { current = localMerge(newClock) ; sender ! current }
        case STOP_GOSSIPER()=> exit
      }
    }
  }
  
  private def increment():VectorClock={
    var currentTimeStamp = System.currentTimeMillis()
    var incrementedEntries = new java.util.ArrayList[ClockEntry]()
    var selfFound = false;
    current.getEntiesList.foreach{clock=>
      if (clock.getNodeID().equals(selfNodeName)) {
        selfFound = true;
        incrementedEntries.add(ClockEntry.newBuilder(clock).setVersion(clock.getVersion() + 1).setTimestamp(currentTimeStamp).build());
      } else {
        incrementedEntries.add(clock);
      }
    }
    if (!selfFound) {
      incrementedEntries.add(ClockEntry.newBuilder().setNodeID(selfNodeName).setVersion(1).setTimestamp(currentTimeStamp).build());
    }
    VectorClock.newBuilder().addAllEnties(incrementedEntries).setTimestamp(currentTimeStamp).build()
  }
  
 // implicit def vectorDebug(vc : VectorClock) = VectorClockAspect(vc) 
  
  private def localMerge(clock2 : VectorClock) : VectorClock ={
    
    var newClockBuilder = VectorClock.newBuilder();
    var clock = current
    var orderedNodeID = new java.util.ArrayList[String]();
    var values = new java.util.HashMap[String, Long]();
    var timestamps = new java.util.HashMap[String, Long]();

    var currentTimeMillis = System.currentTimeMillis();

    var i : Int = 0;
    var j : Int= 0;
    while (i < clock.getEntiesCount() || j < clock2.getEntiesCount()) {
      
      clock match {
        case _ if(i >= clock.getEntiesCount())=> {
            addOrUpdate(orderedNodeID,values,timestamps,clock2.getEnties(j),currentTimeMillis)
            /*
            var v2 = clock2.getEnties(j);
            if (!orderedNodeID.contains(v2.getNodeID())) {
              orderedNodeID.add(v2.getNodeID());
              values.put(v2.getNodeID(), v2.getVersion());
              timestamps.put(v2.getNodeID(), v2.getTimestamp());
            } else {
              values.put(v2.getNodeID(), Math.max(v2.getVersion(), values.get(v2.getNodeID())));
              timestamps.put(v2.getNodeID(), currentTimeMillis);
            }*/
            j = j + 1;
          }
        case _ if(j >= clock2.getEntiesCount())=> {
            addOrUpdate(orderedNodeID,values,timestamps,clock.getEnties(i),currentTimeMillis)
            /*
            var v1 = clock.getEnties(i);
            if (!orderedNodeID.contains(v1.getNodeID())) {
              orderedNodeID.add(v1.getNodeID());
              values.put(v1.getNodeID(), v1.getVersion());
              timestamps.put(v1.getNodeID(), v1.getTimestamp());
            } else {
              values.put(v1.getNodeID(), Math.max(v1.getVersion(), values.get(v1.getNodeID())));
              timestamps.put(v1.getNodeID(), currentTimeMillis);
            }*/
            i= i +1;
          }
        case _ => {
            var v1 = clock.getEnties(i);
            var v2 = clock2.getEnties(j);
            if (v1.getNodeID().equals(v2.getNodeID())) {
              values.put(v1.getNodeID(), Math.max(v1.getVersion(), v2.getVersion()));
              timestamps.put(v1.getNodeID(), currentTimeMillis);
              if (!orderedNodeID.contains(v1.getNodeID())) {
                orderedNodeID.add(v1.getNodeID());
              }
              i= i +1;
              j = j + 1;
            } else {
              if (j < i) {
                if (!orderedNodeID.contains(v2.getNodeID())) {
                  orderedNodeID.add(v2.getNodeID());
                  values.put(v2.getNodeID(), v2.getVersion());
                  timestamps.put(v2.getNodeID(), v2.getTimestamp());
                } else {
                  values.put(v2.getNodeID(), Math.max(v2.getVersion(), values.get(v2.getNodeID())));
                  timestamps.put(v2.getNodeID(), currentTimeMillis);
                }
                j = j + 1;
              } else {
                if (!orderedNodeID.contains(v1.getNodeID())) {
                  orderedNodeID.add(v1.getNodeID());
                  values.put(v1.getNodeID(), v1.getVersion());
                  timestamps.put(v1.getNodeID(), v1.getTimestamp());
                } else {
                  values.put(v1.getNodeID(), Math.max(v1.getVersion(), values.get(v1.getNodeID())));
                  timestamps.put(v1.getNodeID(), currentTimeMillis);
                }
                i= i +1;
              }
            }
          }
      }
    }
    // int index = 0;
    orderedNodeID.foreach{nodeId=>
      var entry = ClockEntry.newBuilder().
      setNodeID(nodeId).
      setVersion(values.get(nodeId)).
      setTimestamp(timestamps.get(nodeId)).build();
      newClockBuilder.addEnties(entry);
    }  
    return newClockBuilder.setTimestamp(currentTimeMillis).build();
  }
  
  
  private def addOrUpdate(orderedNodeID : java.util.ArrayList[String],values:java.util.HashMap[String, Long],timestamps:java.util.HashMap[String, Long],clockEntry : ClockEntry,currentTimeMillis:Long){
    if (!orderedNodeID.contains(clockEntry.getNodeID())) {
      orderedNodeID.add(clockEntry.getNodeID());
      values.put(clockEntry.getNodeID(), clockEntry.getVersion());
      timestamps.put(clockEntry.getNodeID(), clockEntry.getTimestamp());
    } else {
      values.put(clockEntry.getNodeID(), Math.max(clockEntry.getVersion(), values.get(clockEntry.getNodeID())));
      timestamps.put(clockEntry.getNodeID(), currentTimeMillis);
    }
    
  }
  

}
