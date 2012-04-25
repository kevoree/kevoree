package org.kevoree.library.javase.helloworld.${package};

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 27/10/11
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public class HelloProducerThread extends Thread {

    private boolean stopped = false;
    private long delay = 2000;
    private ArrayList<HelloProductionListener> listeners = new ArrayList<HelloProductionListener>();
    private int time = 0;

    public HelloProducerThread() {}

    public HelloProducerThread(long delay) {
        this.delay = delay;
    }

    public void addHelloProductionListener(HelloProductionListener lst) {
        listeners.add(lst);
    }

    public void halt() {
        stopped = true;
    }

    public boolean isStopped(){return stopped;}

    public void run() {
        while(!stopped) {
            produceHello();
            time ++;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void produceHello() {
        for(final HelloProductionListener listener : listeners) {
            new Thread(new Runnable(){
                public void run() {listener.helloProduced("Hello time " + time);}
            }).start();
        }
    }

}
