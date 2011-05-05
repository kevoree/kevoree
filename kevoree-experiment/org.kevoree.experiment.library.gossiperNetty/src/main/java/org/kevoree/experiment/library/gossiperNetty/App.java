package org.kevoree.experiment.library.gossiperNetty;


import org.greg.client.ForkedConfiguration;
import org.greg.client.ForkedGregClient;

public class App {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Tester start");

        ForkedConfiguration clientConfig = new ForkedConfiguration();
        clientConfig.clientId = "duke";
        clientConfig.server = "127.0.0.1";
        clientConfig.calibrationPort = 5677;
        clientConfig.port = 5676;

        ForkedGregClient client = new ForkedGregClient(clientConfig);

        for(int i = 0 ; i < 20 ; i ++ ){
            client.log("hello "+i);
            Thread.sleep(2000);
        }

        client.stop();


    }


}
