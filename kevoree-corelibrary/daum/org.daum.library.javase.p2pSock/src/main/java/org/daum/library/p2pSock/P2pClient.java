package org.daum.library.p2pSock;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 08/08/12
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
public class P2pClient
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Socket requestSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String adr;
    private int port;
    private String remoteNodeName ="";

    public P2pClient(String remoteNodeName,String adr, int port){
        this.adr = adr;
        this.port = port;
        this.remoteNodeName = remoteNodeName;
    }

    void send(Message msg) throws Exception {
        try
        {
            requestSocket = new Socket(adr, port);
            //   System.out.println("Connected to localhost in port 2004");
            //2. get Input and Output streams
            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(msg);

            /*  RichJSONObject c = new RichJSONObject(msg);

            out.writeUTF(c.toJSON());*/
            out.flush();
            out.reset();

        }
        catch(Exception ioException){
            logger.debug("The node '" + remoteNodeName + "' is not available on " + adr + ":" + port);
            throw new Exception(ioException);
        }
        finally{
            //4: Closing connection
            try
            {
                if(requestSocket !=null)
                    requestSocket.close();
                if(out != null)
                    out.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

}