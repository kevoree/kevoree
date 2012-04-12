package org.kevoree.common.gps.impl;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 12/04/12
 * Time: 15:56
 */
public class Tester {

    public static void main(String [] args) throws InterruptedException {
        TracK t = new TracK();
         while (true){
         t.generatePoints(new GpsPoint(),2);
             System.out.println(t.getPoints());
             Thread.sleep(1000);
         }
    }
}
