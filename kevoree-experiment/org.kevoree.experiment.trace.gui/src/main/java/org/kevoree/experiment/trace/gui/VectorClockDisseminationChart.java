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

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

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
	private Map<String, Map<String, String>> vectorClocks;

	private boolean updated;

	public void loadTrace(String traceFilePath) throws IOException {
		File traceFile = new File(traceFilePath);
		if (traceFile.exists()) {
			InputStream input = new FileInputStream(traceFile);
			TraceMessages.Traces traces = TraceMessages.Traces.parseFrom(input);
			loadTrace(traces/*, true*/);
		} else {
			throw new FileNotFoundException(traceFilePath);
		}
	}

	public void saveChart(String chartPath) {
		// TODO

	}

	public void loadTrace(TraceMessages.Traces traces/*, boolean cleanBefore*/) {
		if (/*cleanBefore ||*/ nodeIds == null || timeRepresentations == null || vectorClockUpdates == null || vectorClocks == null) {
			nodeIds = new ArrayList<String>();
			timeRepresentations = new TreeSet<String>();
			vectorClockUpdates = new HashMap<String, List<String>>();
			vectorClocks = new HashMap<String, Map<String, String>>();
		}
		for (TraceMessages.Trace trace : traces.getTraceList()) {
			String nodeId = trace.getMachine() + "_" + trace.getClientId();
			String timeRepresentation = "" + trace.getTimestamp();
			if (!nodeIds.contains(nodeId)) {
				nodeIds.add(nodeId);
			}
			timeRepresentations.add(timeRepresentation);
			List<String> updates = vectorClockUpdates.get(nodeId);
			if (updates == null) {
				updates = new ArrayList<String>();
			}
			updates.add(timeRepresentation);
			vectorClockUpdates.put(nodeId, updates);
			Map<String, String> vectorclockForNode = vectorClocks.get(nodeId);
			if (vectorclockForNode == null) {
				vectorclockForNode = new HashMap<String, String>();
			}
			vectorclockForNode.put(timeRepresentation, trace.getBody());
			vectorClocks.put(nodeId, vectorclockForNode);
		}
		updated = true;
	}

	private CategoryDataset buildPlotDataset() {
		DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
		// TODO
		for (int i = 0; i < nodeIds.size(); i++) {
			long firstTime = 0;
			try {
				firstTime = Long.parseLong(timeRepresentations.iterator().next());
			} catch (NumberFormatException e) {
				e.printStackTrace(); // TODO must not appears
			}
			for (String time : timeRepresentations) {
				String oldTimeRepresentation = time;
				try {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(time) - firstTime);
					time = "" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND);
				} catch (NumberFormatException e) {
					e.printStackTrace(); // TODO must not appears
				}
				if (vectorClockUpdates.get(nodeIds.get(i)).contains(oldTimeRepresentation)) {
					defaultcategorydataset.addValue(i, nodeIds.get(i), time);
				} else {
					defaultcategorydataset.addValue(null, nodeIds.get(i), time);
				}
			}
		}
		return defaultcategorydataset;
	}

	/*private CategoryDataset buildLineDataset() {

	}*/

	private JFreeChart createChart(CategoryDataset categorydataset) {
		JFreeChart jfreechart = ChartFactory.createLineChart("VectorClock updates", "Time", "nodes", categorydataset, PlotOrientation.VERTICAL, false, true, false);
		CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();

		ChartUtilities.applyCurrentTheme(jfreechart);
		LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot.getRenderer();
		String[] nodes = new String[nodeIds.size()];
		for (int i = 0; i < nodeIds.size(); i++) {
			nodes[i] = nodeIds.get(i);
			lineandshaperenderer.setSeriesShapesVisible(i, true); // show plot as a shape
			lineandshaperenderer.setSeriesLinesVisible(i, false); // hide the line
			categoryplot.getRenderer().setSeriesPaint(i, Color.BLACK);// define the black color as default color
		}
		lineandshaperenderer.setBaseItemLabelsVisible(true);
		lineandshaperenderer.setBaseItemLabelGenerator(new VectorClockDisseminationCategoryItemLabelGenerator(nodeIds, timeRepresentations, vectorClocks));
		SymbolAxis symbolaxis = new SymbolAxis("Nodes", nodes);
		categoryplot.setRangeAxis(symbolaxis);


		//lineandshaperenderer.setSeriesShape(2, ShapeUtilities.createDiamond(4F));
		//lineandshaperenderer.setDrawOutlines(true);
		//lineandshaperenderer.setUseFillPaint(true);
		//lineandshaperenderer.setBaseFillPaint(Color.white);

		return jfreechart;
	}

	/*private JFreeChart updateChart(CategoryDataset dataset, JFreeChart chart) {
		
	}*/

	public JFreeChart getChart() {
		if (updated) {
			chart = createChart(buildPlotDataset());
			//chart = updateChart(buildLineDataset(), chart);
			updated = false;
		}
		return chart;
	}
}
