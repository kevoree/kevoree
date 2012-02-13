package org.kevoree.library.android.agrapher;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.achartengine.GraphicalView;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;


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
        @DictionaryAttribute(name = "title", defaultValue = "AGrapher", optional = false),
        @DictionaryAttribute(name = "history_size", defaultValue = "100", optional = false)
        //  @DictionaryAttribute(name = "color_axe", defaultValue = "-1", optional = true),
        // @DictionaryAttribute(name = "color_courbe", defaultValue = "1", optional = true)
})
@ComponentType
public class AGrapher extends AbstractComponentType  {

    KevoreeAndroidService uiService = null;
    Object bundle=null;
    private String title= "";
    private int history_size= 60;
    private int color_axe= Color.WHITE;
    private int color_courbe= Color.RED;

    private LinearLayout layout=null;
    private GraphLine graphline=null;
    // private Logger logger = LoggerFactory.getLogger(AGrapher.class);

    @Start
    public void start() {
        // logger.debug("AGrapher ","starting");

        updateDico();

        bundle = this.getDictionary().get("osgi.bundle");
        //uiService = UIServiceHandler.getUIService((Bundle) bundle);
        graphline = new GraphLine(title,history_size, color_axe,color_courbe);

        layout = new LinearLayout(uiService.getRootActivity());

        uiService.addToGroup("agrapher", layout);

        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run ()
            {

                GraphicalView view= graphline.CreateView(uiService.getRootActivity());
                layout.addView(view);
            }
        });

    }

    @Stop
    public void stop() {
        uiService.remove(layout);
    }

    @Update
    public void update() {
        updateDico();
    }

    public void updateDico(){

        try
        {
            title=              getDictionary().get("title").toString() ;
            history_size=       Integer.parseInt(getDictionary().get("history_size").toString()) ;
            //color_axe=          Integer.parseInt(getDictionary().get("color_axe").toString()) ;
            //color_courbe=       Integer.parseInt(getDictionary().get("color_courbe").toString());
        }  catch (Exception e){
            e.printStackTrace();
        }
    }


    @Port(name = "input")
    public void appendIncoming(Object msg) {

        try {
            double value = Double.parseDouble(msg.toString());
            if(graphline !=null)
                graphline.add(value);
        }  catch (Exception e){
            e.printStackTrace();
        }
    }


}
