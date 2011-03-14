
package org.kevoree.library.gossiper.rest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringZipper {

  /**
   * Gzip the input string into a byte[].
   * @param input
   * @return
   * @throws IOException 
   */
  public static byte[] zipStringToBytes( String input  ) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
    bufos.write( input.getBytes() );
    bufos.flush();
    bufos.close();
    bos.flush();
    byte[] retval= bos.toByteArray();
    bos.close();
    return retval;
  }
  
  /**
   * Unzip a string out of the given gzipped byte array.
   * @param bytes
   * @return
   * @throws IOException 
   */
  public static String unzipStringFromBytes( byte[] bytes ) throws IOException
  {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int len;
    while( (len = bufis.read(buf)) > 0 )
    {
      bos.write(buf, 0, len);
    }
    bos.flush();
    String retval = bos.toString();
    bis.close();
    bufis.close();
    bos.close();
    return retval;
  }
  
  /**
   * Static class.
   *
   */
  private StringZipper()
  {
  }
}