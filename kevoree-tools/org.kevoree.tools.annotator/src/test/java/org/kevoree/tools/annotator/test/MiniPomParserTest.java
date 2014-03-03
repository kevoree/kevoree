package org.kevoree.tools.annotator.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kevoree.DeployUnit;
import org.kevoree.tools.annotator.MinimalPomParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by duke on 28/02/2014.
 */
public class MiniPomParserTest {

    @Test
    public void testRegularPom() throws IOException, SAXException, ParserConfigurationException {
        DeployUnit du = MinimalPomParser.currentURL(getClass().getClassLoader().getResourceAsStream("simplePom.xml"));
        assertEquals("org.kevoree.library.java.sample.breakdown", du.getName());
        assertEquals("org.kevoree.library.java", du.getGroupName());
        assertEquals("1.0-SNAPSHOT", du.getVersion());
    }

    @Test
    public void testPomWithParent() throws IOException, SAXException, ParserConfigurationException {
        DeployUnit du = MinimalPomParser.currentURL(getClass().getClassLoader().getResourceAsStream("inheritancePom.xml"));
        assertEquals("org.kevoree.library.java.helloworld", du.getName());
        assertEquals("org.kevoree.library.java", du.getGroupName());
        assertEquals("3.4.2-SNAPSHOT", du.getVersion());
    }

    @Test
    public void testPomWithParent2() throws IOException, SAXException, ParserConfigurationException {
        DeployUnit du = MinimalPomParser.currentURL(getClass().getClassLoader().getResourceAsStream("inheritance2Pom.xml"));
        assertEquals("org.kevoree.library.java.helloworld", du.getName());
        assertEquals("org.kevoree.library.java", du.getGroupName());
        assertEquals("3.4.2-SNAPSHOT", du.getVersion());
    }

}
