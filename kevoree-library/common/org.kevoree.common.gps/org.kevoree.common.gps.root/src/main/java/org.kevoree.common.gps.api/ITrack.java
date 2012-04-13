package org.kevoree.common.gps.api;

import org.kevoree.common.gps.impl.GpsPoint;

import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 12/04/12
 * Time: 15:07
 */
public interface ITrack {
    public void addPoint(GpsPoint point);
    public void deletePoint(GpsPoint point);
    public void clear();
    public  float getTrackDistance();
    public List<GpsPoint> getPoints();
    public void generatePoints(GpsPoint src,double distance,int nb);
}
