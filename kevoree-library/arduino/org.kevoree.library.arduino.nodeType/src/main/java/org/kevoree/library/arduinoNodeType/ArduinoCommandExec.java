/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author ffouquet
 */
public class ArduinoCommandExec {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ArduinoCommandExec.class);

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
                    logger.debug(s);
                }
                
                // read any errors from the attempted command
                //System.out.println("Here is the standard error of the command (if any):\n");
                while ((s = stdError.readLine()) != null) {
                    logger.debug(s);
                }

                /*p.waitFor();
                //System.exit(0);
            } catch (InterruptedException ex) {
                Logger.getLogger(ArduinoCommandExec.class.getName()).log(Level.SEVERE, null, ex);
            }*/

            //System.out.println("Cmd out !");

        }
        catch (IOException e) {
//            System.out.println("exception happened - here's what I know: ");
//            e.printStackTrace();
			logger.error("exception happened - here's what I know: );", e);
           // System.exit(-1);
        }
        
        
    }
    
}
