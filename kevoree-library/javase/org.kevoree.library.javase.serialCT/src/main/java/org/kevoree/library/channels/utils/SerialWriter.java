package org.kevoree.library.channels.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialWriter extends Thread {

    OutputStream out;
    boolean alive;
    List<String> msg;

    public SerialWriter(OutputStream out) {
        this.out = out;
        this.msg = new ArrayList<String>();
        alive = true;
        //newMsg = false;
    }

    public void sendMsg(String msg){
        this.msg.add(msg);
    }

    public void shutdown() {
        try {
            alive = false;
            sleep(200);
            out.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        //System.out.println("SerialWriter.run");
        try {
            while (alive) {

                if (msg.size() > 0) {
                   // System.out.println("Writing "+msg.get(0)+" on serial port");
                    this.out.write(msg.get(0).getBytes());
                    //newMsg = false;
                    this.out.flush();
                    msg.remove(msg.get(0));
                }
                if (msg.isEmpty()){
                    sleep(100);
                } else {
                    sleep(10);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
