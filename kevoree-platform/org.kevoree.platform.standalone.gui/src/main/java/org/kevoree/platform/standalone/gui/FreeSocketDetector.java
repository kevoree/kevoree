package org.kevoree.platform.standalone.gui;

import java.net.ServerSocket;

/**
 * Created by duke on 3/14/14.
 */
public class FreeSocketDetector {

    public static int detect(int lower, int upper) {
        if (!(lower < upper)) {
            return -1;
        }
        for (int i = lower; i <= upper; i++) {
            try {
                ServerSocket sock = new ServerSocket(i);
                sock.close();
                return i;
            } catch (Exception ex) {
                continue; // try next port
            }
        }
        return -1;
    }

}
