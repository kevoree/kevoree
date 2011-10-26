package org.kevoree.library.socketChannel;


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


