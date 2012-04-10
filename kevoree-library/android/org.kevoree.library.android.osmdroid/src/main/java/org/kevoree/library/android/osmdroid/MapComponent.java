package org.kevoree.library.android.osmdroid;

import android.widget.LinearLayout;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 10/04/12
 * Time: 10:13
 */

@Library(name = "Android")
@Provides(value = {
        @ProvidedPort(name = "zoomIn", type = PortType.MESSAGE),
        @ProvidedPort(name = "zoomOut", type = PortType.MESSAGE),
        @ProvidedPort(name = "pos_map_latitude", type = PortType.MESSAGE),
        @ProvidedPort(name = "pos_map_longitude", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "title", defaultValue = "Map", optional = false)
})
@ComponentType
public class MapComponent extends AbstractComponentType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private KevoreeAndroidService uiService = null;
    private MapView mMapView;
    private MapController mMapController;
    private LinearLayout layout=null;
    private final int sizeTitle = 30;
    private String title= "";
    private double latitude=48117861;
    private double longitude=-1639552;

    @Start
    public void start() {

        uiService = UIServiceHandler.getUIService();
        layout = new LinearLayout(uiService.getRootActivity());

        uiService.addToGroup("Map", layout);

        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run ()
            {

                mMapView = new MapView(uiService.getRootActivity(),sizeTitle);
                mMapView.setTileSource(TileSourceFactory.MAPNIK);

                mMapView.setBuiltInZoomControls(true);
                mMapController = mMapView.getController();
                mMapController.setZoom(8);
                GeoPoint gPt = new GeoPoint(latitude,longitude);
                mMapController.setCenter(gPt);

                layout.addView(mMapView);
            }
        });



    }


    @Stop
    public void stop() {


    }

    @Update
    public void update() {

    }



    public void updateDico(){
        try
        {
            // todo max size 15
            title=              getDictionary().get("title").toString() ;

        }  catch (Exception e){
            logger.error("Fail to update dictionnary "+e.getMessage());
        }
    }


    @Port(name = "zoomIn")
    public void manageZoomIn(Object msg) {
        mMapController.zoomIn();
    }
    @Port(name = "zoomOut")
    public void manageZoomOut(Object msg) {
        mMapController.zoomOut();

    }

    @Port(name = "pos_map_latitude")
    public void pos_map_latitude(Object msg)
    {
        try
        {
            latitude = Double.parseDouble(msg.toString());

        }  catch (Exception e){
            logger.error("Fail to update pos_map_latitude "+e.getMessage());
        }



        GeoPoint gPt = new GeoPoint(latitude,longitude);
        mMapController.setCenter(gPt);
    }

    @Port(name = "pos_map_longitude")
    public void pos_map_longitude(Object msg)
    {
        try
        {
            longitude = Double.parseDouble(msg.toString());

        }  catch (Exception e){
            logger.error("Fail to update pos_map_longitude "+e.getMessage());
        }

        GeoPoint gPt = new GeoPoint(latitude,longitude);
        mMapController.setCenter(gPt);
    }


}
