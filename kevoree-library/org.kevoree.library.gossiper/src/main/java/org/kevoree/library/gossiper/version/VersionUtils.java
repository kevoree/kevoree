/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper.version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;

/**
 *
 * @author ffouquet
 */
public class VersionUtils {

    public static VectorClock merge(VectorClock clock, VectorClock clock2) {
        org.kevoree.library.gossiper.version.GossiperMessages.VectorClock.Builder newClockBuilder = VectorClock.newBuilder();

        List<String> orderedNodeID = new ArrayList();
        Map<String, Long> values = new HashMap<String, Long>();
        Map<String, Long> timestamps = new HashMap<String, Long>();

        int i = 0;
        int j = 0;
        while (i < clock.getEntiesCount() || j < clock2.getEntiesCount()) {

            if (i >= clock.getEntiesCount()) {
                ClockEntry v2 = clock2.getEnties(j);
                if (!orderedNodeID.contains(v2.getNodeID())) {
                    orderedNodeID.add(v2.getNodeID());
                    values.put(v2.getNodeID(), v2.getVersion());
                    timestamps.put(v2.getNodeID(), v2.getTimestamp());
                } else {
                    values.put(v2.getNodeID(), Math.max(v2.getVersion(), values.get(v2.getNodeID())));
                    timestamps.put(v2.getNodeID(), System.currentTimeMillis());
                }
                j++;
                continue;
            }
            if (j >= clock2.getEntiesCount()) {
                ClockEntry v1 = clock.getEnties(i);
                if (!orderedNodeID.contains(v1.getNodeID())) {
                    orderedNodeID.add(v1.getNodeID());
                    values.put(v1.getNodeID(), v1.getVersion());
                    timestamps.put(v1.getNodeID(), v1.getTimestamp());
                } else {
                    values.put(v1.getNodeID(), Math.max(v1.getVersion(), values.get(v1.getNodeID())));
                    timestamps.put(v1.getNodeID(), System.currentTimeMillis());
                }
                i++;
                continue;

            }

            ClockEntry v1 = clock.getEnties(i);
            ClockEntry v2 = clock2.getEnties(j);
            if (v1.getNodeID().equals(v2.getNodeID())) {
                values.put(v1.getNodeID(), Math.max(v1.getVersion(), v2.getVersion()));
                timestamps.put(v1.getNodeID(), System.currentTimeMillis());
                if (!orderedNodeID.contains(v1.getNodeID())) {
                    orderedNodeID.add(v1.getNodeID());
                }
                i++;
                j++;
            } else {
                if (j < i) {
                    if (!orderedNodeID.contains(v2.getNodeID())) {
                        orderedNodeID.add(v2.getNodeID());
                        values.put(v2.getNodeID(), v2.getVersion());
                        timestamps.put(v2.getNodeID(), v2.getTimestamp());
                    } else {
                        values.put(v2.getNodeID(), Math.max(v2.getVersion(), values.get(v2.getNodeID())));
                        timestamps.put(v2.getNodeID(), System.currentTimeMillis());
                    }
                    j++;
                } else {
                    if (!orderedNodeID.contains(v1.getNodeID())) {
                        orderedNodeID.add(v1.getNodeID());
                        values.put(v1.getNodeID(), v1.getVersion());
                        timestamps.put(v1.getNodeID(), v1.getTimestamp());
                    } else {
                        values.put(v1.getNodeID(), Math.max(v1.getVersion(), values.get(v1.getNodeID())));
                        timestamps.put(v1.getNodeID(), System.currentTimeMillis());
                    }
                    i++;
                }
            }
        }

       // int index = 0;
        for (String nodeId : orderedNodeID) {
            ClockEntry entry = ClockEntry.newBuilder().
                    setNodeID(nodeId).
                    setVersion(values.get(nodeId)).
                    setTimestamp(timestamps.get(nodeId)).build();
            newClockBuilder.addEnties(entry);
          //  index++;
        }

        return newClockBuilder.setTimestamp(System.currentTimeMillis()).build();
    }
}
