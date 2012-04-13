package org.kevoree.common.gps.impl;

import org.kevoree.common.gps.api.ITrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 12/04/12
 * Time: 15:04
 */
public class TracK implements ITrack,Serializable {

    private List<GpsPoint> track = new ArrayList<GpsPoint>();

    public void addPoint(GpsPoint point){
        track.add(point);
    }

    public void deletePoint(GpsPoint point){
        track.remove(point);
    }

    public  List<GpsPoint> getPoints(){
        return track;
    }

    public void clear(){

        track.clear();
    }

    public  float getTrackDistance(){
        return 0;
    }


    public String toString(){
        StringBuilder c = new StringBuilder();
        for(GpsPoint p : track){
            c.append(p.toString());
        }
        return c.toString();
    }

    public List<GpsPoint> getTrack() {
        return track;
    }

    public void setTrack(List<GpsPoint> track) {
        this.track = track;
    }

    public void generatePoints(GpsPoint src,double distance,int nb){
        GpsPoint last =src;
        for(int i=0;i<nb;i++)
        {
            GpsPoint current = last.randomPoint(distance);
            addPoint(current);
            last =  current;
        }
    }

}
