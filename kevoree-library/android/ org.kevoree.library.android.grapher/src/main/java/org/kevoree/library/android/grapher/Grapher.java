package org.kevoree.library.android.grapher;

import android.graphics.Color;
import android.view.View;
import android.widget.AbsListView;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;

import org.achartengine.ChartFactory;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.Random;

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
        @DictionaryAttribute(name = "SIZE", defaultValue = "30", optional = true)
})
@ComponentType
public class Grapher extends AbstractComponentType  {

    KevoreeAndroidService uiService = null;
    Object bundle;
    private ScrollView sv;
    private CPUStatusChart cpu;
    private ArrayList<Integer> userdata;
    private ArrayList<Integer> systemdata;

    @Start
    public void start() {

        bundle = this.getDictionary().get("osgi.bundle");
        uiService = UIServiceHandler.getUIService((Bundle) bundle);

        Random rand = new Random();
        userdata    =new ArrayList<Integer>();
        systemdata  =new ArrayList<Integer>();
        for(int i=0;i<100;i++){

            userdata.add  (rand.nextInt(100));
            systemdata.add  (rand.nextInt(100));
        }
        cpu  = new CPUStatusChart();
        sv = new ScrollView(uiService.getRootActivity());

        uiService.addToGroup("grapher", sv);

        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run ()
            {
                View view = null;
                view= cpu.createView(uiService.getRootActivity(),userdata,systemdata);
                view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                sv.removeAllViews();
                sv.addView(view);
            }
        });

    }

    @Stop
    public void stop() {
        uiService.remove(sv);
    }



    @Port(name = "input")
    public void appendIncoming(Object msg) {


    }


}
