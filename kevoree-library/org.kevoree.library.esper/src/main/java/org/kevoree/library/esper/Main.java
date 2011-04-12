package org.kevoree.library.esper;
import java.util.Random;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class Main {

   

    private static Random generator = new Random();

    public static void GenerateRandomTick(EPRuntime cepRT) {

        double price = (double) generator.nextInt(10);
        long timeStamp = System.currentTimeMillis();
        String symbol = "AAPL";
        Tick tick = new Tick(symbol, price, timeStamp);
        System.out.println("Sending tick:" + tick);
        cepRT.sendEvent(tick);

    }


    public static void main(String[] args) {

//The Configuration is meant only as an initialization-time object.
        Configuration cepConfig = new Configuration();
        System.err.println(Tick.class.getName());
        cepConfig.addEventType("StockTick", Tick.class.getName());
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
        EPRuntime cepRT = cep.getEPRuntime();

        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL("select * from " +
                "StockTick(symbol='AAPL').win:length(2) " +
                "having avg(price) > 6.0");

        cepStatement.addListener(new CEPListener());

       // We generate a few ticks...
        for (int i = 0; i < 5; i++) {
            GenerateRandomTick(cepRT);
        }
    }
}
