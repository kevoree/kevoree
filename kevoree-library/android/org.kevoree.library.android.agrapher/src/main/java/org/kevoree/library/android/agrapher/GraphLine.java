package org.kevoree.library.android.agrapher;


import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 09/11/11
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */

public class GraphLine {

    final static String[] titles = new String[] { "User", "System","CPU Usage" };
    static XYSeries systemSeries = new XYSeries(titles[1]);
    static XYSeries userSeries = new XYSeries(titles[0]);
    static final int MAX_POINTS = 60; // 5 mins
    static XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    static XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
    static XYSeriesRenderer renderer1 = new XYSeriesRenderer();
    static XYSeriesRenderer renderer2 = new XYSeriesRenderer();
    static boolean initialized = false;

    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    public String getName() {
        return titles[2];
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return titles[2];
    }

    public void setLineDataToSeries(ArrayList<Double> data,   XYSeries series) {
        series.clear();
        int index = 0;
        for (int i = (data.size() - 1); i >= 0 && index < MAX_POINTS; i--) {
            series.add(index++, data.get(i).doubleValue());
        }
    }

    /**
     * Create a chart view
     *
     * @param context
     *            the context
     * @return the built intent
     */
    public View createView(Context context,ArrayList<Double> data) {
        while (dataset.getSeriesCount() > 0)
            dataset.removeSeries(0);

        setLineDataToSeries(data, userSeries);
        dataset.addSeries(userSeries);

        if (!initialized)
            initialize();

        StringBuffer s = new StringBuffer();
        for (int h = 0; h < userSeries.getItemCount(); h++)
            s.append(userSeries.getY(h)).append(",");
        //Log.i("user data points", s.toString());
        return ChartFactory
                .getLineChartView(context, dataset, renderer);
    }

    private void initialize() {
        renderer1.setColor(Color.RED);
        renderer.addSeriesRenderer(renderer1);

        renderer2.setColor(Color.CYAN);
        renderer.addSeriesRenderer(renderer2);
        // no point styles
        renderer.setChartTitle(titles[2]);
        renderer.setXTitle("Time (s) \u00BB");
        renderer.setYTitle("CPU (%) \u00BB");
        renderer.setXAxisMin(0);// 3x100= 300/ 5 mins
        renderer.setXAxisMax(MAX_POINTS);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(100);
        renderer.setAxesColor(Color.GRAY);
        renderer.setLabelsColor(Color.WHITE);

        renderer.setXLabels(6);
        renderer.setYLabels(10); // every 10%
        renderer.setShowGrid(true);
        // renderer.setAntialiasing(true);
        // renderer.setDisplayChartValues(true);
        initialized = true;

    }

}