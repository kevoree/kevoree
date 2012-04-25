package org.kevoree.library.arduinoNodeType.utils;

/**
 * User: ffouquet
 * Date: 03/06/11
 * Time: 22:03
 */
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author ffouquet
 */
public class SerialSender implements Runnable {

    OutputStream out;
    ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
    public boolean running = true;

    public SerialSender(OutputStream out) {
        this.out = out;
    }

    public void send(String msg) {
        queue.add(msg);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000); //LET MICROCONTROLER REBOOT IF NECESSARY
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (running) {
            if (!queue.isEmpty()) {
                String msg = queue.poll();
                byte[] bs = msg.getBytes();
                try {
                    this.out.write(bs);
                } catch (IOException ex) {
                    queue.add(msg);
                }
            }
        }
        System.out.println("done");
    }
}