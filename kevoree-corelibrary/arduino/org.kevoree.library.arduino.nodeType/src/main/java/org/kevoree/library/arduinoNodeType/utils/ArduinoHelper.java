package org.kevoree.library.arduinoNodeType.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 12/06/12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class ArduinoHelper {

    public static Integer checksumArduino(String value) throws UnsupportedEncodingException
    {
        byte[] data = value.getBytes("US-ASCII");
        long checksum = 0L;
        for( byte b : data )  {
            checksum += b;
        }
        checksum = checksum % 256;
        return new Long( checksum ).intValue();
    }

    /*  version C
        char *checksumArduino( char * buffer ) {
         static char tBuf[4];
         long index;
         unsigned int checksum;
         for( index = 0L, checksum = 0; index < strlen(buffer); checksum += (unsigned int) buffer[index++] );
         sprintf( tBuf, "%03d", (unsigned int) ( checksum % 256 ) );
         return( tBuf );
    }
        public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(ArduinoHelper.checksumArduino("period"));   //  131

    }
     */




}
