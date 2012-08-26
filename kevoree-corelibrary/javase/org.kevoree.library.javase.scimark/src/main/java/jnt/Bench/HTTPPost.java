/*****************************************************************************
jnt.Bench.HTTPPost
 *****************************************************************************/
package jnt.Bench;
import java.net.Socket;
import java.net.URL;
import java.net.ProtocolException;
import java.io.PrintStream;
import java.io.DataInputStream;
/**
HTTPPost posts a message to an HTTP url.

NOTE: Since a socket connection is used, when executed from an Applet,
the security model requires that the server be the same as the server 
from which the applet was loaded.

@author Bruce R. Miller (bruce.miller@nist.gov)
@author Contribution of the National Institute of Standards and Technology,
@author not subject to copyright.
*/
public class HTTPPost {

  /** Post a message to a URL.
    * @param url The url to the HTTP server
    * @param the message text. 
    */
  public static void post(String url, String message) throws Exception {
    URL Url=new URL(url);
    int port = Url.getPort();
    if (port < 0) port=80;
    Socket socket   = new Socket(Url.getHost(), port, true);
    PrintStream output   = new PrintStream(socket.getOutputStream());
    DataInputStream response = new DataInputStream(socket.getInputStream());
    output.println("POST "+Url.getFile()+" HTTP/1.0");
    output.println("Content-Length: "+message.length());
    output.println();
    output.print(message);
    String resp=response.readLine();
    int i0=resp.indexOf(' ')+1;
    int i1=resp.indexOf(' ',i0);
    int retcode = Integer.parseInt(resp.substring(i0,i1).trim());
    if(retcode != 100) throw new ProtocolException(resp);

    output.close();
    socket.close();
  }
}
