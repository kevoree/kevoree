package org.kevoree.library.android.grapher;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;

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
@Provides(value = {
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
    Object bundle=null;
    private ScrollView sv=null;
    private GraphLine cpu=null;
    private ArrayList<Double> data =new ArrayList<Double>();

    @Start
    public void start() {

        bundle = this.getDictionary().get("osgi.bundle");
        uiService = UIServiceHandler.getUIService((Bundle) bundle);

        cpu  = new GraphLine();
        sv = new ScrollView(uiService.getRootActivity());

        uiService.addToGroup("grapher", sv);

        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run ()
            {
                View view = null;
                view= cpu.createView(uiService.getRootActivity(),data);
                view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                sv.removeAllViews();
                sv.addView(view);
            }
        });

    }

    @Stop
    public void stop() {
        uiService.remove(sv);
        data.clear();
    }



    @Port(name = "input")
    public void appendIncoming(Object msg) {

        try {
            double value = Double.parseDouble(msg.toString());
            data.add(value);
        }  catch (Exception e){
            e.printStackTrace();
        }
    }


}
