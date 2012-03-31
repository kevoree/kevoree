package org.kevoree.library.freepastry;

import java.io.IOException;
import java.net.ServerSocket;

public class FreeLocalPort {

    public int getPort() {
        int port = 8181;
        ServerSocket s;
        try {
            s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }
}
