package org.kevoree.library.android.osmdroid;

import android.app.Activity;
import android.graphics.Color;
import org.kevoree.common.gps.impl.GpsPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 12/04/12
 * Time: 15:14
 */
public class TrackMap {

	private PathOverlay pathOverlay;
	private Activity current;
	private MapView mapview;


	public TrackMap(Activity _current,MapView map)
	{
		this.current =_current;
		this.mapview =map;
		pathOverlay = new PathOverlay(Color.RED, current);
		mapview.getOverlays().add(pathOverlay);
	}

	public void addPoint(GpsPoint gps)
	{
		try
		{
			pathOverlay.addPoint(gps.getLatitudeE6(),gps.getLongitudeE6());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void clean(){
		pathOverlay.clearPath();
	}


}