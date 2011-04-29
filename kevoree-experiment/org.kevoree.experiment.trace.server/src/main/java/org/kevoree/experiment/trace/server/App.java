package org.kevoree.experiment.trace.server;

import org.greg.server.ForkedGregServer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class App {

    public static void main(String[] args) {
        System.out.println("Greg Server start ...");

        List<String> gregArgs = Arrays.asList("-port", "5676", "-calibrationPort", "5677");


        ForkedGregServer.startServer(gregArgs.toArray(new String[0]), new File("trace_out"));
    }

}
