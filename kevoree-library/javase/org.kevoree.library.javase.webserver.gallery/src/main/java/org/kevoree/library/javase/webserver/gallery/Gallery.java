package org.kevoree.library.javase.webserver.gallery;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Update;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/10/11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "basedir", defaultValue = "/Users/duke/Pictures")
})
public class Gallery extends AbstractPage {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static byte[] convertStream(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int l;
        do {
            l = (in.read(buffer));
            if (l > 0)
                out.write(buffer, 0, l);
        } while (l > 0);
        return out.toByteArray();
    }

    LoginService loginService = new LoginService();
    AlbumsService albumsService = new AlbumsService();
    ThumbService thumbService = new ThumbService();

    @Override
    public void startPage() {
        update();
        super.startPage();
    }

    @Update
    public void update() {
        albumsService.setBaseDir(new File(this.getDictionary().get("basedir").toString()));
        thumbService.setBaseDir(new File(this.getDictionary().get("basedir").toString()));
        super.updatePage();
    }


    public boolean processService(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (request.getUrl().endsWith("/service/login")) {
            loginService.processService(request, response);
            return true;
        }
        if (request.getUrl().endsWith("/service/albums")) {
            albumsService.processService(request, response);
            return true;
        }
        if (request.getUrl().endsWith("/service/thumb")) {
            thumbService.processService(request, response);
            return true;
        }
        return false;
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (processService(request, response)) {
            return response;
        }
        String file = request.getUrl().substring(request.getUrl().lastIndexOf("/"));
        if (file == null || file.equals("") || file.equals("/")) {
            file = "index.html";
        }
        logger.debug("Request rec for file " + file);
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
        if (in != null) {
            try {
                if (isRaw(request.getUrl())) {
                    response.setRawContent(convertStream(in));
                } else {
                    response.setContent(new String(convertStream(in), "UTF-8"));
                }
//                response.setContentType(getHttpHeaderFromURL(request.getUrl()));
				response.getHeaders().put("Content-Type", getHttpHeaderFromURL(request.getUrl()));
            } catch (Exception e) {
                logger.error("", e);
            }

        } else {
            response.setContent("File not found " + file);
        }
        return response;
    }

    private boolean isRaw(String url) {
        if (url.endsWith(".js")) {
            return false;
        }
        if (url.endsWith(".html")) {
            return false;
        }
        if (url.endsWith(".css")) {
            return false;
        }
        return true;
    }

    private String getHttpHeaderFromURL(String url) {
        if (url.endsWith(".js")) {
            return "text/javascript";
        }
        if (url.endsWith(".html")) {
            return "text/html";
        }
        if (url.endsWith(".css")) {
            return "text/css";
        }
        if (url.endsWith(".png")) {
            return "image/png";
        }
        if (url.endsWith(".gif")) {
            return "image/gif";
        }
        if (url.endsWith(".jpg")) {
            return "image/jpg";
        }
        return "text/html";
    }
}

