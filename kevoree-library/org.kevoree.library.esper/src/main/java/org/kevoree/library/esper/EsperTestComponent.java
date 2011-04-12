/**
 * Project: EnTiMid
 * Copyright: INRIA/IRISA 2011
 * Contributor(s) :
 * Author: barais
 */
package org.kevoree.library.esper;

import java.util.Random;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.RequiredPort;
import org.kevoree.annotation.Requires;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * This Kevoree component encapsulates Esper. 
 * @author Olivier Barais
 * @copyright INRIA
 */

@Requires({
    @RequiredPort(name = "fakeEvent", type = PortType.MESSAGE)
})

@Library(name = "Kevoree::Esper")
@ComponentType(name="Esper Test")
public class EsperTestComponent extends AbstractComponentType {


    public EsperTestComponent(){

    }
   
    @Start
    public void start() {
        for (int i = 0; i < 20; i++) {
            GenerateRandomTick();
        }
        
       
    }

    @Stop
    public void stop() {
    	
    }

    @Update
    public void update() {
        for (int i = 0; i < 20; i++) {
            GenerateRandomTick();
        }
        
    }



	public void send( Tick tick) {
		 if (this.isPortBinded("fakeEvent")) {
	            this.getPortByName("fakeEvent", MessagePort.class).process(tick);
	        }
		
	}
	

    private Random generator = new Random();

    public  void GenerateRandomTick() {

        double price = (double) generator.nextInt(10);
        long timeStamp = System.currentTimeMillis();
        String symbol = "AAPL";
        Tick tick = new Tick(symbol, price, timeStamp);
        System.out.println("Sending tick:" + tick);
        send(tick);

    }

}
