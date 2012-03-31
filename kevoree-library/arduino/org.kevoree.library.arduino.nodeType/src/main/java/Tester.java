import java.io.UnsupportedEncodingException;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 26/03/12
 * Time: 14:58
 */
public class Tester {




    public static Integer checksumArduino(String value) throws UnsupportedEncodingException {
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

     */


    public static void main (String[] args){

        try {
            System.out.println(checksumArduino("HELLO2"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
