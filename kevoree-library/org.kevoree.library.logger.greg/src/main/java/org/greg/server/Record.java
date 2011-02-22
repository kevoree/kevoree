package org.greg.server;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Record {

    public ByteSlice machine;
    public PreciseDateTime serverTimestamp;
    public PreciseDateTime timestamp;
    public ByteSlice message;
    public ByteSlice clientId;

    @Override
    public String toString() {
        try {
            byte[] newline = System.getProperty("line.separator").getBytes("utf-8");
            StringBuilder buffer = new StringBuilder();
            buffer.append(new String(machine.array));
            buffer.append(' ');
            buffer.append(new String(clientId.array));
            buffer.append(' ');
            buffer.append(new String(timestamp.toBytes()));
            buffer.append(' ');
            buffer.append(new String(message.array));
            buffer.append(new String(newline));
            return buffer.toString();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Record.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}
