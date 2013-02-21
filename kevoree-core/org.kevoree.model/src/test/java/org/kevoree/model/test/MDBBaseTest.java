package org.kevoree.model.test;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 20/02/13
* (c) 2013 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.persistency.mdb.PersistentKevoreeFactory;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class MDBBaseTest {


    public static void main(String[] args) {

        PersistentKevoreeFactory factory = new PersistentKevoreeFactory(new File("myModelDb"));

        ContainerRoot modelRoot = factory.createContainerRoot();

        for(int i = 0 ; i < 200; i++) {
            DeployUnit du = factory.createDeployUnit();
            du.setName("du"+ i);
            modelRoot.addDeployUnits(du);


            TypeDefinition td = factory.createTypeDefinition();
            td.setName("td"+ i);
            td.addDeployUnits(du);
            modelRoot.addTypeDefinitions(td);
        }

        assertTrue("Type definition list is not of expected size.", modelRoot.getTypeDefinitions().size()==200);
        assertTrue("Deploy Unit list is not of expected size.", modelRoot.getDeployUnits().size()==200);
        assertTrue("TypeDefinition do not have the good deploy unit.", modelRoot.getTypeDefinitions().get(50).getDeployUnits().get(0).getName().equals("du50"));



    }


}
