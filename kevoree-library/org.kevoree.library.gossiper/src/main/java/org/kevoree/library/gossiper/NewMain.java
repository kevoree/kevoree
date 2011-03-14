/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.gossiper.rest.StringZipper;

/**
 *
 * @author ffouquet
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

            ContainerRoot model = KevoreeFactory.eINSTANCE.createContainerRoot();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("hello");
            KevoreeXmiHelper.saveStream(out, model);
            out.flush();
            String flatModel = new String(out.toByteArray());
            
            String flatZippedModel = new String(StringZipper.zipStringToBytes(flatModel));
            
            String unzippedFlatModel = StringZipper.unzipStringFromBytes(flatZippedModel.getBytes());
            
            out.close();
            
            ContainerRoot model2 = KevoreeXmiHelper.loadStream(new ByteArrayInputStream(unzippedFlatModel.getBytes()));
            
            System.out.println(model2);
            

    }

}
