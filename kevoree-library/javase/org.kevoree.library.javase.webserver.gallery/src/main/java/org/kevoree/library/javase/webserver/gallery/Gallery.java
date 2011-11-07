package org.kevoree.library.javase.webserver.gallery;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/10/11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */
@ComponentType
public class Gallery extends AbstractPage {

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request,KevoreeHttpResponse response){
        //response.setContent(org.kevoree.library.javase.webserver.gallery.SampleFormHelper.generateFormHtml(request.getUrl()));
        return response;
    }

}