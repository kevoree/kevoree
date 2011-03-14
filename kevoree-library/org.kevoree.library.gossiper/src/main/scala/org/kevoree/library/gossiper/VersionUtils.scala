/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock
import org.kevoree.library.gossiper.version.Occured
import scala.collection.JavaConversions._

object VersionUtils {

  def compare(v1 : VectorClock, v2 : VectorClock) : Occured ={
	
	/* If one instance is null => priority to none null version */
	if (v1 == null) {
	  return Occured.BEFORE
	}
	if (v2 == null) {
	  return Occured.AFTER
	}


	// We do two checks: v1 <= v2 and v2 <= v1 if both are true then
	var largerBigger = false
	var smallerBigger = false
	var largerIsV1 = true
	var larger = 0
	var smaller = 0

	var largerClock : VectorClock = null
	var smallerClock : VectorClock = null

	if (v1.getEntiesCount() >= v2.getEntiesCount()) {
	  largerClock = v1
	  smallerClock = v2
	  largerIsV1 = true
	} else {
	  largerClock = v2
	  smallerClock = v1
	  largerIsV1 = false
	}

	for (entry1 <- largerClock.getEntiesList()) {
	  var check = false
	  var ite = smallerClock.getEntiesList().iterator
	  while (!check && ite.hasNext) {
		var entry2 = ite.next
		if (entry1.getNodeID().equals(entry2.getNodeID())) {
		  if (entry1.getVersion() > entry2.getVersion()) {
			largerBigger = true
		  } else if (entry2.getVersion() > entry1.getVersion()) {
			smallerBigger = true
		  }
		  larger = larger + 1
		  smaller = smaller + 1
		  check = true
		}
	  }
		
	  /*for (entry2 <- smallerClock.getEntiesList()) {
	   if (entry1.getNodeID().equals(entry2.getNodeID())) {
	   if (entry1.getVersion() > entry2.getVersion()) {
	   largerBigger = true
	   } else if (entry2.getVersion() > entry1.getVersion()) {
	   smallerBigger = true
	   }
	   larger = larger + 1
	   smaller = larger + 1
	   break
	   }
	   }*/
	   }

	   /* Okay, now check for left overs */
	   if (larger < largerClock.getEntiesCount()) {
		  largerBigger = true
		}
	   if (smaller < smallerClock.getEntiesCount()) {
		  smallerBigger = true
		}

	   /* This is the case where they are equal, return AFTER arbitrarily */
	   //println("larger = " + larger + "smaller = " + smaller)
	   //println("largerBigger : " + largerBigger + " && smallerBigger : " +smallerBigger + " => " + "largerIsV1" + largerIsV1)
	   
	   larger match {
		  case _ if (!largerBigger && !smallerBigger)=>Occured.AFTER
		  case _ if (!largerBigger && smallerBigger) =>Occured.CONCURRENTLY
		  case _ if(largerBigger && !smallerBigger && largerIsV1)=>Occured.AFTER
		  case _ if(largerBigger && !smallerBigger && !largerIsV1)=>Occured.BEFORE
		  case _ if (!largerBigger && smallerBigger && largerIsV1) =>Occured.BEFORE
		  case _ if (!largerBigger && smallerBigger && !largerIsV1) =>Occured.AFTER
		  case _ => Occured.CONCURRENTLY
		}
	
	
	   }
	   }
