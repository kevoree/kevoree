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
        GpsPoint pt = new GpsPoint();
         while (true){

             System.out.println(pt.randomPoint(100));
             Thread.sleep(1000);
             t.clear();
         }
    }
}
