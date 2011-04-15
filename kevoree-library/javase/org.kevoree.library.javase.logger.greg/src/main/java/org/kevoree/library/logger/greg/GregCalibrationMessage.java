/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.logger.greg;

import java.util.UUID;
import org.greg.server.TimeSpan;

/**
 *
 * @author ffouquet
 */
public class GregCalibrationMessage {

    private UUID uuid;

    private TimeSpan timeSpan;

    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
