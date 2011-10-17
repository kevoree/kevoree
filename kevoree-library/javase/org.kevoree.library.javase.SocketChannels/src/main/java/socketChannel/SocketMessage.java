package socketChannel;



import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.*;

import   org.kevoree.framework.message.*;


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 11/10/11
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class SocketMessage extends org.kevoree.framework.message.Message{

    private   String uuid;

    public String getUuid(){

        return uuid;
    }

    public void setUuid(String uuid){

        this.uuid =uuid;
    }


}


