/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.xmpp;

/**
 *
 * @author ffouquet
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here

        System.out.println("Hello");
        XmppComponent compo = new XmppComponent();

        compo.start();

        for(int i=0;i<10;i++){

            compo.sendMessage("YERPE");

            Thread.sleep(1000);
        }

        compo.stop();
    }

}
