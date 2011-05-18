/*
package org.kevoree.experiment.trace.gui;

import org.jfree.chart.ChartPanel;
import org.kevoree.experiment.trace.TraceMessages;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;

public class AppSample {

    public static void main(String[] args) throws IOException, URISyntaxException {

        InputStream input = AppSample.class.getClassLoader().getResourceAsStream("./trace_out");

        TraceMessages.Traces traces = TraceMessages.Traces.parseFrom(input);

        for (TraceMessages.Trace trace : traces.getTraceList()) {

            //UNSERIALIZE VECTOR CLOCK

            String[] clockEntries = trace.getBody().split(",");
            for (int i = 0; i < clockEntries.length; i++) {
                String[] values = clockEntries[i].split(":");
                if (values.length >= 2) {
                    System.out.println(values[0] + "=>" + values[1].trim());
                    Integer.parseInt(values[1].trim());
                }
            }
        }

        JFrame frame = new JFrame();
        frame.setSize(400, 400);

        VectorClockDisseminationChart chart = new VectorClockDisseminationChart();
        //chart.loadTrace(traces*//*
*/
/*, true*//*
*/
/*);
        chart.loadTrace(new File(AppSample.class.getClassLoader().getResource("./trace_out").toURI()).getAbsolutePath());


        ChartPanel chartPanel = new ChartPanel(chart.getChart());
        chartPanel.setOpaque(false);
        frame.add(chartPanel);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);


    }

    public static String convertStreamToString(InputStream is)
            throws IOException {
        */
/*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         *//*

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
*/
