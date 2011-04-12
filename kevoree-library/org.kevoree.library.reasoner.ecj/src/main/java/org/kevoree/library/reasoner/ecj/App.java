package org.kevoree.library.reasoner.ecj;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreePackage;
import org.kevoree.MBinding;
import org.kevoree.NamedElement;
import org.kevoree.Port;
import org.kevoree.library.reasoner.ecj.dpa.AddComponentDPA;
import org.kevoree.tools.marShell.ast.Script;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext;
import org.kevoree.tools.marShell.parser.KevsParser;
import org.kevoree.tools.marShell.parser.ParserUtil;
import org.kevoree.tools.ui.editor.KevoreeEditor;

import scala.Option;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {

        System.out.println("Hello World!");
        

        ContainerRoot newModel = load("target/classes/defaultLibrary.kev");
        System.out.println(new AddComponentDPA().applyPointcut(newModel));
        System.out.println("Communication Delay : " + evaluateCommunicationDelay(newModel));
        displayFitness(newModel);
        startEditor("target/classes/defaultLibrary.kev");
    }
    
    
//    public static void main(String[] args) {
//        
//        System.out.println("Hello World!");
//
//
//        ContainerRoot newModel = load("target/classes/base.kev");
//        
//        KevsParser parser = new KevsParser();
//        Option<Script> oscript = parser.parseScript(ParserUtil.loadFile("target/classes/addNode.kevs"));
//
//        if (!oscript.isEmpty()) {
//            Script script = (Script) oscript.get();
//
//            System.out.println("script" + script);
//
//            KevsInterpreterContext context = new KevsInterpreterContext(newModel);
//
//            KevsInterpreterAspects.rich(script).interpret(context);
//
//            ParserUtil.save("target/classes/modified.kev", newModel);
//
//           
//
//        } else {
//            //System.out.println(parser.lastNoSuccess());
//        }
//
//    }
    
    
    private static float evaluateCommunicationDelay(ContainerRoot newModel) {
        float numberOfInterNodesBindings = 0;
        float totalNumberOfBindings = newModel.getMBindings().size();
        if (totalNumberOfBindings <= 1){
            return 100;
        }
        for (MBinding myBinding : newModel.getMBindings()) {
            for (MBinding myBinding2 : newModel.getMBindings()) {
                if (!myBinding.equals(myBinding2) && myBinding.getHub().equals(myBinding2.getHub()) && ((isRequired(myBinding.getPort()) && !isRequired(myBinding2.getPort())) || (!isRequired(myBinding.getPort()) && isRequired(myBinding2.getPort())))){
                    // test if the component  
                    
                    if (!myBinding.getPort().eContainer().eContainer().equals(myBinding2.getPort().eContainer().eContainer())){
                        System.out.println("My binding1 : " + ((NamedElement)myBinding.getPort().eContainer()).getName() + "." + ((NamedElement)myBinding.getPort().getPortTypeRef()).getName() + ", my binding 2 : " + ((NamedElement)myBinding2.getPort().eContainer()).getName() + "." + ((NamedElement)myBinding2.getPort().getPortTypeRef()).getName());
                        numberOfInterNodesBindings++;
                    }
                }
            }
        }
        numberOfInterNodesBindings = numberOfInterNodesBindings/2;
        if (numberOfInterNodesBindings == 0){
            return 100;
        }
        return 100 - ((numberOfInterNodesBindings*100)/totalNumberOfBindings);
    }
    
 // return true if this port is a required port, false if it is a provided port.
    private static boolean isRequired(Port port) {
        for (Port requiredPort : ((ComponentInstance)port.eContainer()).getRequired()) {
            if (requiredPort.equals(port)){
                return true;
            }
        }
        return false;
    }

    // Will give a good score to architecture which emphasize load balancing on every available components
    private static float evaluateLoadBalancing(ContainerRoot newModel) {
        if (newModel.getNodes().size() > 0) {
            int[] myNodes = new int[newModel.getNodes().size()];
            int i = 0;
            for (ContainerNode myNode : newModel.getNodes()) {
                myNodes[i] = myNode.getComponents().size();
                i++;
            }
            int min = myNodes[0];
            int max = myNodes[0];
            for (int numberOfComponents : myNodes) {
                if (min > numberOfComponents) {
                    min = numberOfComponents;
                }
                if (max < numberOfComponents) {
                    max = numberOfComponents;
                }
            }
            if (max > 0) {
                return 100 - (((max - min) * 100) / max);
            } else {
                return 100;
            }
        }
        return 100;
    }

    public static ContainerRoot load(String uri) {
        ResourceSetImpl rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("kev", new XMIResourceFactoryImpl());
        rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
        Resource res = rs.getResource(URI.createURI(uri), true);
        ((XMIResource) res).getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        ((XMIResource) res).getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        Object result = res.getContents().get(0);
        //println(res)
        return (ContainerRoot) result;
    }

    public static void startEditor(String modelPath) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        JFrame jframe = new JFrame("Kevoree Editor");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setPreferredSize(new Dimension(800, 600));
        KevoreeEditor artpanel = new KevoreeEditor();

        jframe.setJMenuBar(artpanel.getMenuBar());


        jframe.add(artpanel.getPanel(), BorderLayout.CENTER);
        jframe.pack();
        jframe.setVisible(true);

        artpanel.loadModel(modelPath);
    }
    
    public static void displayFitness(ContainerRoot newModel){
        if (newModel.getNodes().size()!= 1){
            System.out.println("Fitness = " + 0);
            return;
        }
        //components evaluation
        ContainerNode cn = newModel.getNodes().get(0);
        
        if (cn.getComponents().size()>3 || newModel.getHubs().size()>3){
            System.out.println("Fitness = " + 0);
            return;
        }
        double componentNumberMultiplier = 0.0 ;
        if (cn.getComponents().size()<=3) {
            componentNumberMultiplier = cn.getComponents().size() * 33.3;
        }
        double componentTypeMultiplier = 40.0;
        boolean fakeLight = false, fakeSwitch = false, fakeConsole = false;
        for (ComponentInstance myComp : cn.getComponents()) {
            if (myComp.getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") && !fakeLight){
                componentTypeMultiplier += 20;
                fakeLight = true;
            }
            if (myComp.getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch") && !fakeSwitch){
                componentTypeMultiplier += 20;
                fakeSwitch = true;
            }
            if (myComp.getTypeDefinition().getName().equalsIgnoreCase("FakeConsole") && !fakeConsole){
                componentTypeMultiplier += 20;
                fakeConsole = true;
            }
        }
     // channels evaluation
        double channelNumberMultiplier = 10 + (newModel.getHubs().size() * 30) ;
        if (channelNumberMultiplier > 100){
            channelNumberMultiplier = 100;
        }
//        if (ki.myModel.getHubs().size()<=3) {
//            channelNumberMultiplier = 10 + (ki.myModel.getHubs().size() * 30);
//        }
        double channelTypeMultiplier = 40.0;
        for (Channel myChannel : newModel.getHubs()) {
            if (myChannel.getTypeDefinition().getName().equalsIgnoreCase("defMSG")){
                if (channelTypeMultiplier+20 <=100)
                        channelTypeMultiplier += 20;
            }
        }
        
        //bindings evaluation
        double bindingNumberMultiplier = 0 ;
        if (newModel.getMBindings().size()<=5) {
            bindingNumberMultiplier = newModel.getMBindings().size() * 20;
        }
//        double bindingTypeMultiplier = 40.0;
//        boolean onToOnBinding = false, onToShowTextBinding = false, offToOffBinding = false;
//        for (MBinding myBinding : newModel.getMBindings()) {
//            System.out.println("Binding component : " + ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName() + " :  port : " + myBinding.getPort().getPortTypeRef().getName() + " => Channel : " + myBinding.getHub().getName());
//            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") &&  !onToOnBinding){
//                System.out.println("Should pass here for binding = FakeSimpleLight:on");
//                System.out.println("---");
//                for (MBinding myBinding1 : newModel.getMBindings()){
//                    if (!myBinding.equals(myBinding1)){
//                        System.out.println("Binding component : " + ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName() + " :  port : " + myBinding.getPort().getPortTypeRef().getName() + " => Channel : " + myBinding.getHub().getName());
//                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
//                            bindingTypeMultiplier += 20;
//                            onToOnBinding = true;
//                        }
//                    }
//                }
//                System.out.println("---");
//            }
//            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch") && !onToShowTextBinding){
//                for (MBinding myBinding1 : newModel.getMBindings()){
//                    if (!myBinding.equals(myBinding1)){
//                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("showText") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeConsole")){
//                            bindingTypeMultiplier += 20;
//                            onToShowTextBinding = true;
//                        }
//                    }
//                }
//            }
//            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight") && !offToOffBinding){
//                for (MBinding myBinding1 : newModel.getMBindings()){
//                    if (!myBinding.equals(myBinding1)){
//                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
//                            bindingTypeMultiplier += 20;
//                            offToOffBinding = true;
//                        }
//                    }
//                }
//            }
//            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
//                for (MBinding myBinding1 : newModel.getMBindings()){
//                    if (!myBinding.equals(myBinding1)){
//                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
//                            bindingTypeMultiplier = 0;
//                            onToOnBinding = true;
//                            break;
//                        }
//                    }
//                }
//            }
//            if (myBinding.getPort().getPortTypeRef().getName().equalsIgnoreCase("off") && ((ComponentInstance)myBinding.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleLight")){
//                for (MBinding myBinding1 : newModel.getMBindings()){
//                    if (!myBinding.equals(myBinding1)){
//                        if (myBinding.getHub().equals(myBinding1.getHub()) && myBinding1.getPort().getPortTypeRef().getName().equalsIgnoreCase("on") && ((ComponentInstance)myBinding1.getPort().eContainer()).getTypeDefinition().getName().equalsIgnoreCase("FakeSimpleSwitch")){
//                            bindingTypeMultiplier = 0;
//                            onToOnBinding = true;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
        
        double fitness = 50.0*(componentNumberMultiplier/100.0)*(componentTypeMultiplier/100.0);
        fitness += 50.0*(channelNumberMultiplier/100.0)*(channelTypeMultiplier/100.0);
//        fitness += 20.0*(bindingNumberMultiplier/100.0)*(bindingTypeMultiplier/100.0);
        Double f = new Double(fitness);
        System.out.println("ComponentNumberMultiplier : " + componentNumberMultiplier);
        System.out.println("componentTypeMultiplier : " + componentTypeMultiplier);
        System.out.println("channelNumberMultiplier : " + channelNumberMultiplier);
        System.out.println("channelTypeMultiplier : " + channelTypeMultiplier);
        System.out.println("bindingNumberMultiplier : " + bindingNumberMultiplier);
//        System.out.println("bindingTypeMultiplier : " + bindingTypeMultiplier);
        
        System.out.println("Fitness = " + fitness);
    }
}
