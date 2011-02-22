/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.greg.client;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ffouquet
 */
public class GregTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        for(int i=0;i<10;i++){
            Greg.log("helloGregKevoree");
            System.out.println("SayHelloToGreg");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GregTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
