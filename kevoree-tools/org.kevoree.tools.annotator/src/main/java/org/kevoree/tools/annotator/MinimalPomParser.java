package org.kevoree.tools.annotator;

import org.kevoree.DeployUnit;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by duke on 06/02/2014.
 */
public class MinimalPomParser {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {

        File pomF = new File("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.annotator/pom.xml");
        System.out.println(currentURL(pomF));
    }

    public static DeployUnit currentURL(File mavenPom) throws ParserConfigurationException, IOException, SAXException {

        DeployUnit du = new DefaultKevoreeFactory().createDeployUnit();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(mavenPom);
        doc.getDocumentElement().normalize();

        String artifactId = doc.getElementsByTagName("artifactId").item(0).getTextContent();
        NodeList res = doc.getElementsByTagName("groupId");
        String groupId = null;
        if (res.getLength() == 1) {
            groupId = res.item(0).getTextContent();
        } else {
            NodeList parentL = doc.getElementsByTagName("parent");
            if (parentL.getLength() == 1) {
                groupId = ((Element) parentL.item(0)).getElementsByTagName("groupId").item(0).getTextContent();
            }
        }
        String version = null;
        res = doc.getElementsByTagName("version");
        if (res.getLength() == 1) {
            version = res.item(0).getTextContent();
        } else {
            NodeList parentL = doc.getElementsByTagName("parent");
            if (parentL.getLength() == 1) {
                version = ((Element) parentL.item(0)).getElementsByTagName("version").item(0).getTextContent();
            }
        }

        du.setGroupName(groupId);
        du.setName(artifactId);
        du.setVersion(version);

        return du;
    }

}
