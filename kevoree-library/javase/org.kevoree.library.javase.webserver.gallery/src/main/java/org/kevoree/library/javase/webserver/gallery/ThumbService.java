package org.kevoree.library.javase.webserver.gallery;

import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 10/11/11
 * Time: 06:35
 * To change this template use File | Settings | File Templates.
 */
public class ThumbService {

    File baseDir = null;

    public void setBaseDir(File b) {
        baseDir = b;
    }

    public void processService(KevoreeHttpRequest request, KevoreeHttpResponse response) {

        if (!baseDir.exists()) {
            return;
        }

//    Image img = ImageIO.read(new File("test.jpg")).getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);
    }

}
