package org.kevoree.experiment.trace.gui;

import org.jfree.chart.ChartPanel;
import org.kevoree.experiment.trace.TraceMessages;

import javax.swing.*;
import java.awt.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/04/11
 * Time: 10:52
 */
public class App {

	public static void main(String[] args) {
		TraceMessages.Traces.Builder tracesBuilder = TraceMessages.Traces.newBuilder();

		TraceMessages.Trace.Builder traceBuilder = TraceMessages.Trace.newBuilder();
		traceBuilder.setMachine("localhost");
		traceBuilder.setClientId("duke");
		traceBuilder.setTimestamp(System.currentTimeMillis() - 10000);
		traceBuilder.setBody("ma vectorClock0");

		tracesBuilder.addTrace(traceBuilder.build());

		traceBuilder = TraceMessages.Trace.newBuilder();
		traceBuilder.setMachine("localhost");
		traceBuilder.setClientId("duke2");
		traceBuilder.setTimestamp(System.currentTimeMillis());
		traceBuilder.setBody("ma vectorClock1");

		tracesBuilder.addTrace(traceBuilder.build());

		traceBuilder = TraceMessages.Trace.newBuilder();
		traceBuilder.setMachine("localhost");
		traceBuilder.setClientId("duke2");
		traceBuilder.setTimestamp(System.currentTimeMillis() - 5000);
		traceBuilder.setBody("ma vectorClock2");

		tracesBuilder.addTrace(traceBuilder.build());


		JFrame frame = new JFrame();
		frame.setSize(400, 400);

		VectorClockDisseminationChart chart = new VectorClockDisseminationChart();
		chart.loadTrace(tracesBuilder.build()/*, true*/);

		ChartPanel chartPanel = new ChartPanel(chart.getChart());
		chartPanel.setOpaque(false);
		frame.add(chartPanel);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
