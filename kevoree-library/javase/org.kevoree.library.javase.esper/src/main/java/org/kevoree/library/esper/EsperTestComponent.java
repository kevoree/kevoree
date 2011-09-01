/**
 * Project: EnTiMid
 * Copyright: INRIA/IRISA 2011
 * Contributor(s) :
 * Author: barais
 */
package org.kevoree.library.esper;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * This Kevoree component encapsulates Esper. 
 * @author Olivier Barais
 * @copyright INRIA
 */

@Requires({
    @RequiredPort(name = "fakeEvent", type = PortType.MESSAGE)
})


@Library(name = "Kevoree::Esper")
@ComponentType
public class EsperTestComponent extends AbstractComponentType {

	private Logger logger = LoggerFactory.getLogger(EsperTestComponent.class);

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
        logger.info("Sending tick:" + tick);
        send(tick);
    }

}
