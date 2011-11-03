package org.kevoree.library.grapher.android;

import com.androidplot.series.XYSeries;


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 02/11/11
 * Time: 15:36
 * To change this template use File | Settings | File Templates.
 */

public class DynamicSeries implements XYSeries {
    private DynamicXYDatasource datasource;
    private int seriesIndex;
    private String title;

    public DynamicSeries(DynamicXYDatasource datasource, int seriesIndex, String title) {
        this.datasource = datasource;
        this.seriesIndex = seriesIndex;
        this.title = title;
    }
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int size() {
        return datasource.getItemCount();
    }

    @Override
    public Number getX(int index) {
        return datasource.getX(index);
    }

    @Override
    public Number getY(int index) {
        return datasource.getY(index);
    }
}