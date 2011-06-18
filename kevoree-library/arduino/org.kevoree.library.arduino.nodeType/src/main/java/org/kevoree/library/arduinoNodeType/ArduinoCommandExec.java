/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ffouquet
 */
public class ArduinoCommandExec {

    static String s = null;
    public static void execute(String cmd){
        try {
            
	    // run the Unix "ps -ef" command
            // using the Runtime exec method:
            //System.out.println(cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            
            BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
           // try {
                // read the output from the command
                //System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }
                
                // read any errors from the attempted command
                //System.out.println("Here is the standard error of the command (if any):\n");
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

                /*p.waitFor();
                //System.exit(0);
            } catch (InterruptedException ex) {
                Logger.getLogger(ArduinoCommandExec.class.getName()).log(Level.SEVERE, null, ex);
            }*/

            //System.out.println("Cmd out !");

        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
           // System.exit(-1);
        }
        
        
    }
    
}
