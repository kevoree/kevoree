package org.kevoree.library.grapher.android;

import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import com.androidplot.Plot;
import com.androidplot.xy.*;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 02/11/11
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

/**
 * @author jed
 */
@Library(name = "Android")
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "title", defaultValue = "Grapher", optional = true),
        @DictionaryAttribute(name = "HISTORY_SIZE", defaultValue = "30", optional = true),
        @DictionaryAttribute(name = "SIZE", defaultValue = "30", optional = true),
})
@ComponentType
public class Grapher extends AbstractComponentType   {

    KevoreeAndroidService uiService = null;
    Object bundle;
    private FrameLayout layout = null;
    private XYPlot dynamicPlot;
    private XYPlot staticPlot;
    private MyPlotUpdater plotUpdater;
    private  DynamicXYDatasource data;
    @Start
    public void start() {

        bundle = this.getDictionary().get("osgi.bundle");
        uiService = UIServiceHandler.getUIService((Bundle) bundle);
        //create the TTS instance
        // The OnInitListener (second argument) is called after initialization completes.
        //mTts = new TextToSpeech(uiService.getRootActivity(), this);

        uiService.addToGroup("layout", layout);
        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run () {
                apply(layout);
            }
        });

    }

    @Stop
    public void stop() {

    }


    private void apply(FrameLayout view) {

        layout.addView(dynamicPlot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);

        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        data = new DynamicXYDatasource(Integer.parseInt(getDictionary().get("SIZE").toString()),Integer.parseInt(getDictionary().get("HISTORY_SIZE").toString()));
        DynamicSeries sine1Series = new DynamicSeries(data, 0,  getDictionary().get("title").toString());


        // create a series using a temporary formatter, with a transparent fill applied immediately
        dynamicPlot.addSeries(sine1Series, new LineAndPointFormatter(Color.rgb(0, 0, 0), null, null));
        dynamicPlot.setTitle( getDictionary().get("title").toString());
        // create a series using an instantiated formatter, with some transparency applied after creation:
        LineAndPointFormatter form1 = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(0, 0, 80));
        form1.getFillPaint().setAlpha(220);

        dynamicPlot.setGridPadding(5, 0, 5, 0);

        // hook up the plotUpdater to the data model:
        data.addObserver(plotUpdater);

        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        dynamicPlot.setDomainStepValue(sine1Series.size());

        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();

        // freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);
               // kick off the data generating thread:


    }

    // redraws a plot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;
        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }
        @Override
        public void update(Observable o, Object arg) {
            try {
                plot.postRedraw();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    @Port(name = "input")
    public void appendIncoming(Object msg) {

        data.addItem((Number) msg);

    }


}
