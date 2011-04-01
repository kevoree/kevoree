package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;

@NodeType
@Library(name = "KevoreeNodeType")
public class ArduinoNode extends AbstractNodeType {

    @Override
    public void push(String targetNodeName, ContainerRoot root) {
        System.out.println("I'm the arduino deployer");

        //STEP 0 : FOUND ARDUINO COMMUNICATION CHANNEL

      //  ContainerRoot newModel = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-library/org.kevoree.library.arduino.nodeType/src/test/resources/models/TempSensorAlone.kev");

        // Generator.generate(containerRoot);
        KevoreeKompareBean kompare = new KevoreeKompareBean();
        AdaptationModel kompareModel = kompare.kompare(KevoreeFactory.eINSTANCE.createContainerRoot(), root, targetNodeName);


        //STEP 1 : GENERATE FLAT CODE - MODEL SPECIFIQUE
        //STEP 2 : COMPILE to PDE Target
        deploy(kompareModel, targetNodeName);


        //STEP 3 : Deploy by commnication channel


    }



    @Override
    public boolean deploy(AdaptationModel model, String nodeName) {

         Generator.generate(model,nodeName);

        return true;
    }


}
