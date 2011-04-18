package org.kevoree.library.channels.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.library.channels.SerialCT;

public class SerialReader extends Thread {

    InputStream in;
    boolean alive;
    SerialCT channelFragment;

    public SerialReader(InputStream in, SerialCT channel) {
        this.in = in;
        this.channelFragment = channel;
        alive = true;
    }

    public void shutdown() {
        try {
            alive = false;
            sleep(200);
            in.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void restart(){
        shutdown();
        channelFragment.restartSerialReader();
    }

    public void run() {
        //System.out.println(device.getClass().getName()+".SerialReader.run");
        while (alive) {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                String data = "";
                while ((len = this.in.read(buffer)) > -1) {
                    data += new String(buffer, 0, len);
                    if (data.endsWith("\n") || data.endsWith("\u0003") || data.endsWith("\u0a0a")) {
                        break;
                    }
                }
                device.push(data.trim().replace("\u0002", "").replace("\u0003", "").replace("\u0a0d", ""));
                sleep(100);
                //System.out.println();
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
                restart();
            } catch (IOException ex) {
                 Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
                 restart();
            }
        }
        //System.out.println(device.getClass().getName()+".SerialReader has been shut down");
    }
}