package org.kevoree.experiment.trace.gui;

import org.kevoree.experiment.trace.TraceMessages;

import java.io.IOException;
import java.io.InputStream;

public class AppSample {

    public static void main(String[] args) throws IOException {

        InputStream input = AppSample.class.getClassLoader().getResourceAsStream("./trace_out");

        TraceMessages.Traces traces = TraceMessages.Traces.parseFrom(input);


        for (TraceMessages.Trace trace : traces.getTraceList()) {

            //UNSERIALIZE VECTOR CLOCK

            String[] clockEntries = trace.getBody().split(",");
            for (int i = 0; i < clockEntries.length; i++) {
                String[] values = clockEntries[i].split(":");
                if (values.length >= 2) {
                    System.out.println(values[0] + "=>" + values[1]);
                }
            }
        }


        //


    }


}
