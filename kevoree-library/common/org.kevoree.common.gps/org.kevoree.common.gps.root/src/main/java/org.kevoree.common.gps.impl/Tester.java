package org.kevoree.common.gps.impl;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 12/04/12
 * Time: 15:56
 */
public class Tester {

    public static void main(String [] args) throws InterruptedException {
         while (true){
             System.out.println(new GpsPoint());
             Thread.sleep(1000);
         }
    }
}
