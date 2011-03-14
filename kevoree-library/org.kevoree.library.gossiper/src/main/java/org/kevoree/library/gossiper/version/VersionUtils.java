/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper.version;

import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;

/**
 *
 * @author ffouquet
 */
public class VersionUtils {

	/**
	 * Return Occured.After if v1 is more recent than v2
	 * Return
	 * @param v1
	 * @param v2
	 * @return Occured.After if v1 is more recent than v2 or v1 is equals to v2, Occured.BEFORE if v2 is more recent than v1, Occured.CONCURRENTLY otherwise
	 */
	public static Occured compare(VectorClock v1, VectorClock v2) {
		/* If one instance is null => priority to none null version */
		if (v1 == null) {
			return Occured.BEFORE;
		}
		if (v2 == null) {
			return Occured.AFTER;
		}


		// We do two checks: v1 <= v2 and v2 <= v1 if both are true then
		boolean largerBigger = false;
		boolean smallerBigger = false;
		boolean largerIsV1;
		int larger = 0;
		int smaller = 0;

		VectorClock largerClock;
		VectorClock smallerClock;

		if (v1.getEntiesCount() >= v2.getEntiesCount()) {
			largerClock = v1;
			smallerClock = v2;
			largerIsV1 = true;
		} else {
			largerClock = v2;
			smallerClock = v1;
			largerIsV1 = false;
		}


		for (ClockEntry entry1 : largerClock.getEntiesList()) {
			//boolean compared = false;
			for (ClockEntry entry2 : smallerClock.getEntiesList()) {
				if (entry1.getNodeID().equals(entry2.getNodeID())) {
					if (entry1.getVersion() > entry2.getVersion()) {
						largerBigger = true;
					} else if (entry2.getVersion() > entry1.getVersion()) {
						smallerBigger = true;
					}
					larger++;
					smaller++;
					break;
				}
			}
			/*if (!compared) {
			largerBigger = true;
			}*/
		}

		/* Okay, now check for left overs */
		if (larger < largerClock.getEntiesCount()) {
			largerBigger = true;
		} else if (smaller < smallerClock.getEntiesCount()) {
			smallerBigger = true;
		}

		/* This is the case where they are equal, return AFTER arbitrarily */
		if (!largerBigger && !smallerBigger) {
			return Occured.AFTER;
		} /* This is the case where v1 is a successor clock to v2 */ else if (largerBigger && !smallerBigger) {
			if (largerIsV1) {
				return Occured.AFTER;
			} else {
				return Occured.BEFORE;
			}
		} /* This is the case where v2 is a successor clock to v1 */ else if (!largerBigger && smallerBigger) {
			if (largerIsV1) {
			return Occured.BEFORE;
			} else {
				return Occured.AFTER;
			}
		} /* This is the case where both clocks are parallel to one another */ else {
			return Occured.CONCURRENTLY;
		}











		/*while (p1 < v1.getEntiesCount() /*|| p2 < v2.getEntiesCount()*) {
		ClockEntry ver1 = v1.getEnties(p1);
		while (p2 < v2.getEntiesCount()) {
		ClockEntry ver2 = v2.getEnties(p2);
		if (ver1.getNodeID().equals(ver2.getNodeID())) {
		if (ver1.getVersion() > ver2.getVersion()) {
		v1Bigger = true;
		} else if (ver2.getVersion() > ver1.getVersion()) {
		v2Bigger = true;
		}
		p1++;
		p2++;
		} else {
		p2++;
		}
		}
		p1++;
		}
		/* Okay, now check for left overs *
		if (p1 < v1.getEntiesCount()) {
		v1Bigger = true;
		} else if (p2 < v2.getEntiesCount()) {
		v2Bigger = true;
		}
		
		/* This is the case where they are equal, return BEFORE arbitrarily *
		if (!v1Bigger && !v2Bigger) {
		return Occured.AFTER;
		} /* This is the case where v1 is a successor clock to v2 * else if (v1Bigger && !v2Bigger) {
		return Occured.AFTER;
		} /* This is the case where v2 is a successor clock to v1 * else if (!v1Bigger && v2Bigger) {
		return Occured.BEFORE;
		} /* This is the case where both clocks are parallel to one another * else {
		return Occured.CONCURRENTLY;
		}*/








		/*while (p1 < v1.getEntiesCount() || p2 < v2.getEntiesCount()) {
		// while (p1 < v1.getEntiesCount() && p2 < v2.getEntiesCount()) {
		ClockEntry ver1 = v1.getEnties(p1);
		ClockEntry ver2 = v2.getEnties(p2);
		if (ver1.getNodeID().equals(ver2.getNodeID())) {
		if (ver1.getVersion() > ver2.getVersion()) {
		v1Bigger = true;
		} else if (ver2.getVersion() > ver1.getVersion()) {
		v2Bigger = true;
		}
		p1++;
		p2++;
		
		//Completement faux dans notre cas , on doit travailler sur les distances et non sur un index d'ordre total !
		} else if (p1 > p2) {
		// since ver1 is bigger that means it is missing a version that
		// ver2 has
		v2Bigger = true;
		p2++;
		} else {
		// this means ver2 is bigger which means it is missing a version
		// ver1 has
		v1Bigger = true;
		p1++;
		}
		}
		
		/* Okay, now check for left overs 
		if (p1 < v1.getEntiesCount()) {
		v1Bigger = true;
		} else if (p2 < v2.getEntiesCount()) {
		v2Bigger = true;
		}
		
		/* This is the case where they are equal, return BEFORE arbitrarily 
		if (!v1Bigger && !v2Bigger) {
		return Occured.AFTER;
		} /* This is the case where v1 is a successor clock to v2  else if (v1Bigger && !v2Bigger) {
		return Occured.AFTER;
		} /* This is the case where v2 is a successor clock to v1  else if (!v1Bigger && v2Bigger) {
		return Occured.BEFORE;
		} /* This is the case where both clocks are parallel to one another *else {
		return Occured.CONCURRENTLY;
		}*/
	}
}
