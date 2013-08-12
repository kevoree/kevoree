package org.kevoree.tools.ui.editor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Created by duke on 12/08/13.
 */
public class KevoreeStore {

    public static void main(String[] args){
        KevoreeStore store = new KevoreeStore();
        store.getFromGroupID(null);
    }


    public void getFromGroupID(String groupIDparam){

        URL url = null;
        try {
            url = new URL("http://oss.sonatype.org/service/local/data_index?g=org.kevoree.corelibrary*");
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            NodeList nList = doc.getElementsByTagName("artifact");

            System.out.println(nList.getLength());

            is.close();


        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
