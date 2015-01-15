package org.kevoree.annotator;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by duke on 06/02/2014.
 */
public class MinimalPomParser {

    public static DeployUnit lookupLocalDeployUnit(File classPath, ContainerRoot root, KevoreeFactory factory) {
        try {
            if (classPath.exists()) {
                if (classPath.getName().equals("classes")) {
                    File parent = classPath.getParentFile();
                    if (parent != null && parent.getName().equals("target")) {
                        File parent2 = parent.getParentFile();
                        if (parent2 != null) {
                            File mavenPom = new File(parent2, "pom.xml");
                            return currentURL(mavenPom, root, factory);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error("", e);
        }
        return null;
    }


    public static DeployUnit currentURL(File mavenPom, ContainerRoot root, KevoreeFactory factory) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return currentURL(dBuilder.parse(mavenPom), root, factory);
    }

    public static DeployUnit currentURL(InputStream mavenPom, ContainerRoot root, KevoreeFactory factory) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return currentURL(dBuilder.parse(mavenPom), root, factory);
    }


    public static DeployUnit currentURL(Document doc, ContainerRoot root, KevoreeFactory factory) throws ParserConfigurationException, IOException, SAXException {

        DeployUnit du = new DefaultKevoreeFactory().createDeployUnit();
        doc.getDocumentElement().normalize();

        XPath xpath = XPathFactory.newInstance().newXPath();
        String artifactId = null;
        String groupId = null;
        String version = null;


        try {
            artifactId = xpath.evaluate("//project/artifactId", doc.getDocumentElement(), XPathConstants.STRING).toString();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        if (artifactId == null || artifactId.equals("")) {
            try {
                artifactId = xpath.evaluate("//project/parent/artifactId", doc.getDocumentElement(), XPathConstants.STRING).toString();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        try {
            groupId = xpath.evaluate("//project/groupId", doc.getDocumentElement(), XPathConstants.STRING).toString();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        if (groupId == null || groupId.equals("")) {
            try {
                groupId = xpath.evaluate("//project/parent/groupId", doc.getDocumentElement(), XPathConstants.STRING).toString();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }

        try {
            version = xpath.evaluate("//project/version", doc.getDocumentElement(), XPathConstants.STRING).toString();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        if (version == null || version.equals("")) {
            try {
                version = xpath.evaluate("//project/parent/version", doc.getDocumentElement(), XPathConstants.STRING).toString();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        String[] packages = groupId.split("\\.");
        org.kevoree.Package previous = null;
        for (int i = 0; i < packages.length; i++) {
            if (previous == null) {
                previous = root.findPackagesByID(packages[i]);
                if (previous == null) {
                    previous = factory.createPackage();
                    previous.setName(packages[i]);
                    root.addPackages(previous);
                }
            } else {
                org.kevoree.Package resolved = previous.findPackagesByID(packages[i]);
                if (resolved == null) {
                    resolved = factory.createPackage();
                    resolved.setName(packages[i]);
                    previous.addPackages(resolved);
                    previous = resolved;
                } else {
                    previous = resolved;
                }
            }
        }
        if (previous != null) {
            du.setName(artifactId);
            du.setVersion(version);
            previous.addDeployUnits(du);
        } else {
            Log.error("DeployUnit need a qualified name {}", groupId);
        }
        return du;
    }

}
