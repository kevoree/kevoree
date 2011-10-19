/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

/**
 *
 * @author sunye
 */
public class Main {

    public static void main(String[] args) throws Exception {

        HadoopNameNode nameNode = new HadoopNameNode();
        nameNode.start();

        HadoopDataNode dataNode = new HadoopDataNode();
        dataNode.start();

        HadoopJobTracker tracker = new HadoopJobTracker();
        tracker.start();
    }
}
