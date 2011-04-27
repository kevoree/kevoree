package org.kevoree.experiment.trace.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.kevoree.experiment.trace.TraceMessages;

import java.io.*;
import java.util.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/04/11
 * Time: 10:49
 */
public class VectorClockDisseminationChart {

	private JFreeChart chart;

	private List<String> nodeIds;
	private Set<String> timeRepresentations;
	private Map<String, List<String>> vectorClockUpdates;

	private boolean updated;

	public void loadTrace(String traceFilePath) throws IOException {
		File traceFile = new File(traceFilePath);
		if (traceFile.exists()) {
			InputStream input = new FileInputStream(traceFile);
			TraceMessages.Traces traces = TraceMessages.Traces.parseFrom(input);
			loadTrace(traces/*, true*/);
			System.out.println("traces loaded");
		} else {
			throw new FileNotFoundException(traceFilePath);
		}
	}

	public void saveChart(String chartPath) {
		// TODO
	}

	public void loadTrace(TraceMessages.Traces traces/*, boolean cleanBefore*/) {
		if (/*cleanBefore ||*/ nodeIds == null || timeRepresentations == null || vectorClockUpdates == null) {
			nodeIds = new ArrayList<String>();
			timeRepresentations = new TreeSet<String>();
			vectorClockUpdates = new HashMap<String, List<String>>();
		}
		for (TraceMessages.Trace trace : traces.getTraceList()) {
			String nodeId = trace.getMachine() + "_" + trace.getClientId();
			if (!nodeIds.contains(nodeId)) {
				nodeIds.add(nodeId);
			}
			timeRepresentations.add("" + trace.getTimestamp());
			List<String> updates = vectorClockUpdates.get(nodeId);
			if (updates == null) {
				updates = new ArrayList<String>();
			}
			updates.add("" + trace.getTimestamp());
			vectorClockUpdates.put(nodeId, updates);
		}
		updated = true;
	}

	private CategoryDataset buildDataset() {
		DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
		// TODO
		for (int i = 0; i < nodeIds.size(); i++) {
			for (String time : timeRepresentations) {
				if (vectorClockUpdates.get(nodeIds.get(i)).contains(time)) {
					defaultcategorydataset.addValue(i, nodeIds.get(i), time);
				} else {
					defaultcategorydataset.addValue(null, nodeIds.get(i), time);
				}
			}
		}
		return defaultcategorydataset;
	}

	private JFreeChart createChart(CategoryDataset categorydataset) {
		JFreeChart jfreechart = ChartFactory.createLineChart("VectorClock updates", "Time", "Count", categorydataset, PlotOrientation.VERTICAL, false, true, false);
		CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();

		ChartUtilities.applyCurrentTheme(jfreechart);
		LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot.getRenderer();
		String[] nodes = new String[nodeIds.size()];
		for (int i = 0; i < nodeIds.size(); i++) {
			nodes[i] = nodeIds.get(i);
			lineandshaperenderer.setSeriesShapesVisible(i, true);
			lineandshaperenderer.setSeriesLinesVisible(i, false);
		}
		SymbolAxis symbolaxis = new SymbolAxis("Nodes", nodes);
		categoryplot.setRangeAxis(symbolaxis);


		//lineandshaperenderer.setSeriesShape(2, ShapeUtilities.createDiamond(4F));
		//lineandshaperenderer.setDrawOutlines(true);
		//lineandshaperenderer.setUseFillPaint(true);
		//lineandshaperenderer.setBaseFillPaint(Color.white);

		return jfreechart;
	}

	public JFreeChart getChart() {
		if (updated) {
			chart = createChart(buildDataset());
			updated = false;
		}
		return chart;
	}
}
