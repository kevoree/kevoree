package org.kevoree.library.javase.webserver.sample;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/03/12
 * Time: 23:21
 */
@ComponentType
public class StaticFilesPage extends ParentAbstractPage {

    protected String basePage = "hello.html";
    
    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {

        if (FileServiceHelper.checkStaticFile(basePage, this, request, response)) {
            if (request.getUrl().equals("/") || request.getUrl().endsWith(".html")) {
                String pattern = getDictionary().get("urlpattern").toString();
                if(pattern.endsWith("**")){
                    pattern = pattern.replace("**","");
                }
                if (!pattern.endsWith("/")) {
                    pattern = pattern + "/";
                }

                if(response.getRawContent() != null){
                    response.setRawContent(new String(response.getRawContent()).replace("{urlpattern}", pattern).getBytes());
                } else {
                    response.setContent(response.getContent().replace("{urlpattern}", pattern));
                }
            }
            return response;
        }
        response.setContent("Bad request");
        return response;
    }

}
