package org.kevoree.library;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/12
 * Time: 14:38
 */
public class TesterBroadCastAsk {

    public static void main(String[] args){
        BroadCastSender.send(8000,"8HelloUDP".getBytes());
    }

}
