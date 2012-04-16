package org.kevoree.library.android.osmdroid;

import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.common.gps.impl.GpsPoint;
import org.kevoree.common.gps.impl.TracK;
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
        @ProvidedPort(name = "positionMAP", type = PortType.MESSAGE),
        @ProvidedPort(name = "track", type = PortType.MESSAGE),
        @ProvidedPort(name = "cleartrack", type = PortType.MESSAGE)
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
    private final int sizeTitle = 15;
    private String title= "";
    private TrackMap track;
    private  LowMemory lowMemory = new LowMemory();

    @Start
    public void start() {

        uiService = UIServiceHandler.getUIService();
        layout = new LinearLayout(uiService.getRootActivity());
        lowMemory.setLowMemory();
        uiService.addToGroup("Map", layout);

        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run ()
            {
                mMapView = new MapView(uiService.getRootActivity(),sizeTitle);
                mMapView.setBuiltInZoomControls(true);
                mMapController = mMapView.getController();
                mMapController.setZoom(16);
                GeoPoint gPt = new GeoPoint(48096397, -1743137);
                mMapController.setCenter(gPt);
                mMapView.setTileSource(TileSourceFactory.MAPNIK);
                mMapView.setActivated(true);
                track = new TrackMap(uiService.getRootActivity(), mMapView);

                DisplayMetrics dm = new DisplayMetrics();
                uiService.getRootActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

                mMapView.setMinimumHeight(dm.heightPixels);
                mMapView.setMinimumWidth(dm.widthPixels);
                layout.addView(mMapView);
            }
        });

    }


    public TrackMap getTrack() {
        return track;
    }


    public void setTrack(TrackMap track) {
        this.track = track;
    }

    @Stop
    public void stop() {

        mMapView = null;
        uiService.remove(layout);
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


    @Port(name = "positionMAP")
    public void positionMAP(Object msg)
    {
        if(msg instanceof GpsPoint)
        {
            GpsPoint tmp = (GpsPoint) msg;
            GeoPoint gPt = new GeoPoint(tmp.getLatitudeE6(),tmp.getLongitudeE6());
            mMapController.setCenter(gPt);

        } else {
            logger.error("WTF !");
        }
    }

    @Port(name = "track")
    public void manage_track(final Object msg)
    {
        if(msg instanceof TracK)
        {
            uiService.getRootActivity().runOnUiThread(new Runnable() {
                @Override
                public void run ()
                {
                    TracK tmp = (TracK) msg;
                    for(GpsPoint p : tmp.getPoints())
                    {
                        if (!lowMemory.isLowMemory())
                        {
                            track.addPoint(p);
                            mMapController.animateTo(new GeoPoint(p.getLat(),p.getLong_()));
                            mMapView.invalidate();
                        }else
                        {
                            // TODO save track

                            track.clean();

                        }
                    }
                }
            });

        } else
        {
            logger.error("WTF !");
        }

    }


    @Port(name = "cleartrack")
    public void cleartrack(Object msg)
    {
        getTrack().clean();
    }




}
