package org.kevoree.library.javase.webserver.gallery;

import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 10/11/11
 * Time: 06:22
 * To change this template use File | Settings | File Templates.
 */
public class LoginService {

    public void processService(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (!request.getResolvedParams().containsKey("user")) {
            response.setContent("user expected");
        }
        if (!request.getResolvedParams().containsKey("pass")) {
            response.setContent("password expected");
        }
        String user = request.getResolvedParams().get("user");
        String pass = request.getResolvedParams().get("pass");

        //HARD CODE

        if(user.equals("admin") && pass.equals("admin")){
            response.setContent("<loginack />");
        }
        
    }

}
