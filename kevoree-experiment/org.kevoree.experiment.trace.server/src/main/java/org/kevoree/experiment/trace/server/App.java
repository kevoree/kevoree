package org.kevoree.experiment.trace.server;

import org.greg.server.Configuration;
import org.greg.server.GregServer;
import org.greg.server.Trace;

import java.util.Arrays;
import java.util.List;

public class App {

   public static void main(String [] args)
	{
       System.out.println("Greg Server start ...");

        List<String> gregArgs = Arrays.asList("-port","5676","-calibrationPort","5677");


       GregServer.main(gregArgs.toArray(new String[0]));
    }

}
