package org.kevoree.common.api;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 10/04/12
 * Time: 16:02
 */
public interface IGpsPoint {

    IGpsPoint fromDoubleString(final String s, final char spacer);
    IGpsPoint fromIntString(final String s);

    int getLat();
    int getLong_();
    int distanceTo(final IGpsPoint src,final IGpsPoint dest) ;
    void setLongitudeE6(final int aLongitudeE6) ;
    void setLatitudeE6(final int aLatitudeE6);
    void setCoordsE6(final int aLatitudeE6, final int aLongitudeE6);
}
