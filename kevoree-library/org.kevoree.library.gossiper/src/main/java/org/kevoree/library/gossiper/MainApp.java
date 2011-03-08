/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper;

import org.kevoree.library.gossiper.version.GossiperMessages;

/**
 *
 * @author ffouquet
 */
public class MainApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Elle");

        GossiperMessages.VectorClock vector = GossiperMessages.VectorClock.newBuilder().build();

    }

}
