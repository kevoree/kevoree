package org.kevoree.tools.emf.compat;

import org.kevoree.framework.KevoreeXmiHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/10/12
 * Time: 13:14
 */
public class Tester {

    public static void main(String[] args) throws IOException {

        Integer nbTest = 10;
        Long beginTime = System.currentTimeMillis();
        System.out.println("Begin ->");
        File input = new File("/Users/duke/Desktop/arduinoLedSimple.kev");
        for (int i = 0; i < nbTest; i++) {
            org.kemf.compat.kevoree.ContainerRoot model = EMFXmiHelper.loadStream(new FileInputStream(input));
            File temp = File.createTempFile("yop","yop");
            temp.deleteOnExit();
            FileOutputStream fout = new FileOutputStream(temp);
            EMFXmiHelper.saveStream(fout,model);
            fout.close();
            input = temp;

        }
        System.out.println("EMF AVG = "+((System.currentTimeMillis()-beginTime)/nbTest));
        beginTime = System.currentTimeMillis();
        for (int i = 0; i < nbTest; i++) {
            org.kevoree.ContainerRoot model = KevoreeXmiHelper.loadStream(new FileInputStream("/Users/duke/Desktop/arduinoLedSimple.kev"));

        }
        System.out.println("KMF AVG = "+((System.currentTimeMillis()-beginTime)/nbTest));
    }

}
