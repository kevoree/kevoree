package org.kevoree.common.gps.impl;


import org.kevoree.common.gps.api.GeoConstants;
import org.kevoree.common.gps.api.IGpsPoint;
import org.kevoree.common.gps.api.MathConstants;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 10/04/12
 * Time: 16:04
 */
public class GpsPoint implements IGpsPoint {

    protected static final String LAT_EDEFAULT = null;
    protected static final String LONG_EDEFAULT = null;
    private int lat;
    private int long_;
    private int satellites_used ;
    private int mode ;
    private int altitude ;
    private float speed ;
    private float track ;


    public GpsPoint(){
        // random base on RENNES
        double lat =    48.115683+ (Math.random() * 0.1 );
        double lon =         -1.664286+ (Math.random() * 0.1);
        this.lat =  (int)(lat* 1E6);
        this.long_ =  (int)(lon* 1E6);
    }

    public double meters2degrees_lat(double meters) {
        return meters / (2.0*Math.PI*6356752.3142/360.0);
    }
    public double meters2degrees_lon(double meters) {
        return (double)((meters / (2.0*Math.PI*6378137.0/360.0)));
    }

    public double feet2meters(double feet) {
        return feet * 0.3048006096;
    }
    public double meters2feet(double meters) {
        return meters / 0.3048006096;
    }

    public  double deg2rad2(double degrees) { /* convert degrees to radians */
        return degrees * (Math.PI/180.0);
    }

    public  double rad2deg2(double radians) { /* convert radian to degree */
        return radians * (180.0/Math.PI);
    }


    int Random (int _iMin, int _iMax)
    {
        return (int)(Math.random() * (_iMax-_iMin)) + _iMin;
    }


    public GpsPoint randomPoint()
    {
        double width, height,distance;
        int yaw = Random(0,360);
        double lat;
        double lon;
        distance = 100;
        if (yaw< 90)
        {
            width  =  distance * Math.sin(deg2rad2(yaw));
            height =  distance * Math.cos(deg2rad2(yaw));
        } else if (yaw < 180) {
            width  =  distance * Math.cos(deg2rad2(yaw-90));
            height = -distance * Math.sin(deg2rad2(yaw-90));
        } else if (yaw < 270) {
            width  = -distance * Math.sin(deg2rad2(yaw-180));
            height = -distance * Math.cos(deg2rad2(yaw-180));
        } else {
            width  = -distance * Math.cos(deg2rad2(yaw-270));
            height =  distance * Math.sin(deg2rad2(yaw-270));
        }

        lat  =(this.lat /1E6)+ meters2degrees_lat(height);
        lon = (this.long_ /1E6)+  meters2degrees_lon(width);

        this.lat =  (int)(lat* 1E6);
        this.long_ =  (int)(lon* 1E6);
        return  this;
    }


    public GpsPoint(String latitude,String longitude,String satellites_used ,
                    String mode ,
                    String altitude ,
                    String speed ,
                    String track){
        try
        {
            this.lat = (int)(Double.parseDouble(latitude) * 1E6);
            this.long_ = (int)(Double.parseDouble(longitude) * 1E6);
            this.satellites_used = Integer.parseInt(satellites_used);
            this.mode = Integer.parseInt(mode);
            this.speed = Float.parseFloat(speed);
            this.track = Float.parseFloat(track);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public GpsPoint(final int aLatitudeE6, final int aLongitudeE6) {
        this.lat = aLatitudeE6;
        this.long_ = aLongitudeE6;
    }

    public GpsPoint(final double aLatitude, final double aLongitude) {
        this.lat = (int)(aLatitude * 1E6);
        this.long_ = (int)(aLongitude * 1E6);
    }



    public  IGpsPoint fromDoubleString(final String s, final char spacer) {
        final int spacerPos = s.indexOf(spacer);
        return new GpsPoint((int) (Double.parseDouble(s.substring(0,
                spacerPos - 1)) * 1E6), (int) (Double.parseDouble(s.substring(
                spacerPos + 1, s.length())) * 1E6));
    }

    public IGpsPoint fromIntString(final String s){
        final int commaPos = s.indexOf(',');
        return new GpsPoint(Integer.parseInt(s.substring(0,commaPos-1)),
                Integer.parseInt(s.substring(commaPos+1,s.length())));
    }

    public int getLongitudeE6() {
        return this.long_;
    }

    public int getLatitudeE6() {
        return this.lat;
    }

    public void setLongitudeE6(final int aLongitudeE6) {
        this.long_ = aLongitudeE6;
    }

    public void setLatitudeE6(final int aLatitudeE6) {
        this.lat = aLatitudeE6;
    }

    public void setCoordsE6(final int aLatitudeE6, final int aLongitudeE6) {
        this.lat = aLatitudeE6;
        this.long_ = aLongitudeE6;
    }

    public String toWaypoint() {
        return new StringBuilder().append(this.lat / 1E6).append(":").append(this.long_ / 1E6).toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        final GpsPoint rhs = (GpsPoint)obj;
        return rhs.lat == this.lat && rhs.long_ == this.long_;
    }

    public int distanceTo(final IGpsPoint src,final IGpsPoint dest) {

        final double a1 = MathConstants.DEG2RAD * (src.getLat() / 1E6);
        final double a2 = MathConstants.DEG2RAD * (src.getLong_() / 1E6);
        final double b1 = MathConstants.DEG2RAD * (dest.getLat() / 1E6);
        final double b2 = MathConstants.DEG2RAD * (dest.getLong_() / 1E6);
        final double cosa1 = Math.cos(a1);
        final double cosb1 = Math.cos(b1);
        final double t1 = cosa1*Math.cos(a2)*cosb1*Math.cos(b2);
        final double t2 = cosa1*Math.sin(a2)*cosb1*Math.sin(b2);
        final double t3 = Math.sin(a1)*Math.sin(b1);
        final double tt = Math.acos( t1 + t2 + t3 );
        return (int)(GeoConstants.RADIUS_EARTH_METERS*tt);
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(" (lat: ");
        result.append(lat);
        result.append(", long: ");
        result.append(long_);
        result.append(')');
        return result.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLong_() {
        return long_;
    }

    public void setLong_(int long_) {
        this.long_ = long_;
    }

    public int getSatellites_used() {
        return satellites_used;
    }

    public void setSatellites_used(int satellites_used) {
        this.satellites_used = satellites_used;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTrack() {
        return track;
    }

    public void setTrack(Float track) {
        this.track = track;
    }

    public static String getLatEdefault() {
        return LAT_EDEFAULT;
    }

    public static String getLongEdefault() {
        return LONG_EDEFAULT;
    }

}
