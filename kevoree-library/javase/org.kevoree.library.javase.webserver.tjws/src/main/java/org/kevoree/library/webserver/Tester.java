package org.kevoree.library.webserver;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/04/12
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class Tester {

    public static void main(String[] args) throws InterruptedException {
        KTinyWebServer server = new KTinyWebServer();
        server.getDictionary().put("port","8080");
        server.start();
    }

}
