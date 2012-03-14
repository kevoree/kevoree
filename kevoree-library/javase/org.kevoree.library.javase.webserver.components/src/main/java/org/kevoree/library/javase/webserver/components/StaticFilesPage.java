package org.kevoree.library.javase.webserver.components;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/03/12
 * Time: 23:21
 */
@ComponentType
public class StaticFilesPage extends AbstractPage {

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (FileServiceHelper.checkStaticFile("hello.html", this, request, response)) {
            return response;
        }
        response.setContent("Bad request");
        return response;
    }

}
