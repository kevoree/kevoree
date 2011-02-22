/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.logger.greg;

import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import org.greg.server.Record;

/**
 *
 * @author ffouquet
 */
public class GregRecordsMessage {

    private UUID uuid;

    private List<Record> records;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    private SocketAddress adress;

    public SocketAddress getAdress() {
        return adress;
    }

    public void setAdress(SocketAddress adress) {
        this.adress = adress;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

}
