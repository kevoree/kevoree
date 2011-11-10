package org.kevoree.library.javase.webserver.gallery;

import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 10/11/11
 * Time: 06:29
 * To change this template use File | Settings | File Templates.
 */
public class AlbumsService {

    File baseDir = null;

    public void setBaseDir(File b) {
        baseDir = b;
    }

    public void processService(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (!baseDir.exists()) {
            return;
        }

        if (request.getResolvedParams().containsKey("filename")) {
           File subdir = new File(baseDir.getAbsolutePath()+File.separator+request.getResolvedParams().get("filename"));
           if(subdir.exists()){
               File[] subFiles =subdir.listFiles(new FileFilter() {
                   @Override
                   public boolean accept(File file) {
                       if(file.getName().endsWith(".png")||file.getName().endsWith(".jpg")){
                           return true;
                       } else {
                           return false;
                       }
                   }
               });
               StringBuffer buf = new StringBuffer();
               buf.append("{");
               for (int i = 0; i < subFiles.length; i++) {
                   buf.append("\""+i+"\":");
                   buf.append("{\"id\":\""+subFiles[i].getName()+"\",");
                   buf.append(",\"thumbnail\":\"service/thumb?filename=" + request.getResolvedParams().get("filename") + "/" + subFiles[i].getName() + "\"");
               //    buf.append(",\"fullsize\":\"script/" + $albumDir + "/" . $file . "\"");

                   buf.append(",\"ofullsize\":\"service/thumb?filename=" + request.getResolvedParams().get("filename") + "/" + subFiles[i].getName() + "\"");
                   buf.append("}");
                   if(i != subFiles.length-1){buf.append(",");}



             //      echo "\"id\":\"" . $i.$_GET['filename'].$file. "\"";
             //      echo ",\"thumbnail\":\"script/thumb.php?filename=" . $_GET['filename'] . "/" . $file . "\"";
             //      //echo ",\"fullsize\":\"script/" . $albumDir . "/" . $file . "\"";
             //      echo ",\"fullsize\":\"script/othumb.php?filename=" . $_GET['filename'] . "/" . $file . "\"";
             //      echo ",\"ofullsize\":\"script/" . $albumDir . "/" . $file . "\"";

               }
               buf.append("}");
               response.setContent(buf.toString());
               response.setContentType("text/json");
           }
        } else {
            StringBuffer buf = new StringBuffer();
            File[] files = baseDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    if (buf.length() > 0) {
                        buf.append(";");
                    }
                    buf.append(files[i].getName());
                }
            }
            response.setContent("<dirs>" + buf.toString() + "</dirs>");
        }

    }

}
