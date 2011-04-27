package org.kevoree.experiment.trace.gui;

import org.kevoree.experiment.trace.TraceMessages;

import java.io.*;

public class AppSample {

    public static void main(String[] args) throws IOException {

        InputStream input = AppSample.class.getClassLoader().getResourceAsStream("./trace_out");

            TraceMessages.Traces traces = TraceMessages.Traces.parseFrom(input);

            System.out.println(traces.getTraceCount());



     //


    }


    public static String convertStreamToString(InputStream is)
            throws IOException {
        /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
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
