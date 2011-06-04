package org.kevoree.library.arduinoNodeType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.osgi.framework.BundleContext;
import org.wayoda.ang.libraries.CodeManager;
import org.wayoda.ang.libraries.Core;
import org.wayoda.ang.project.ArduinoBuildEnvironment;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;
import org.wayoda.ang.project.TargetDirectoryService;

import javax.swing.*;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.arduinoNodeType.generator.KevoreeCGenerator;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.kevoreeAdaptation.TypeAdaptation;

@NodeType
@Library(name = "KevoreeNodeType")
@DictionaryType({
    @DictionaryAttribute(name = "boardTypeName", defaultValue = "uno", optional = true),
    @DictionaryAttribute(name = "boardPortName"),
    @DictionaryAttribute(name = "incremental", defaultValue = "true", optional = true)
})
public class ArduinoNode extends AbstractNodeType {

    ArduinoGuiProgressBar progress = null;
    File newdir = null;
    
    @Override
    public void push(final String targetNodeName, final ContainerRoot root, final BundleContext bundle) {

        new Thread() {

            @Override
            public void run() {
                progress = new ArduinoGuiProgressBar();
                JFrame frame = new JFrame("Arduino model push");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setContentPane(progress);
                frame.pack();
                frame.setVisible(true);

                bcontext = bundle;


                progress.beginTask("Build diff model", 10);
                KevoreeKompareBean kompare = new KevoreeKompareBean();

                newdir = new File("arduinoGenerated" + targetNodeName);
                if (!newdir.exists()) {
                    newdir.mkdir();
                }

                ContainerRoot lastVersionModel = KevoreeFactory.eINSTANCE.createContainerRoot();

                int lastVersion = 0;
                //Try to find previous version
                if (getDictionary().get("incremental") != null && getDictionary().get("incremental").equals("true")) {

                    System.out.println("Incremental search");
                    
                    
                    File lastModelFile = null;
                    for (File f : newdir.listFiles()) {
                        if (f.getName().endsWith(".kev")) {
                            try {
                                String nameWithoutExtention = f.getName().substring(0, f.getName().lastIndexOf("."));
                                String version = nameWithoutExtention.substring(nameWithoutExtention.lastIndexOf("_") + 1);
                                Integer nversion = Integer.parseInt(version);
                                if (nversion > lastVersion) {
                                    lastVersion = nversion;
                                    lastModelFile = f;
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    try {
                        lastVersionModel = KevoreeXmiHelper.load("file:///" + lastModelFile.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                AdaptationModel kompareModel = kompare.kompare(lastVersionModel, root, targetNodeName);
                progress.endTask();


                progress.beginTask("Prepare model generation", 20);
                File newdirTarget = new File("arduinoGenerated" + targetNodeName + "/target");
                newdirTarget.mkdir();
                TargetDirectoryService.rootPath = newdirTarget.getAbsolutePath();

                outputPath = newdir.getAbsolutePath();
                System.out.println("outDir=" + outputPath);
                progress.endTask();

                progress.beginTask("Compute firmware update", 30);
                try {
                    if (deploy(kompareModel, targetNodeName)) {
                        progress.endTask();
                    } else {
                        progress.failTask();
                    }
                } catch (Exception e) {
                    progress.failTask();
                    e.printStackTrace();
                }



                progress.beginTask("Save model for incremental deployment", 100);
                KevoreeXmiHelper.save("file:///" + newdir.getAbsolutePath() + "/" + targetNodeName + "_" + (lastVersion + 1) + ".kev", root);
                progress.endTask();

                frame.setVisible(false);
                frame.dispose();


            }
        }.start();


    }
    private String outputPath = "";
    private BundleContext bcontext = null;

    
    @Override
    public boolean deploy(AdaptationModel model, String nodeName) {
        boolean typeAdaptationFound = false;
        for (AdaptationPrimitive p : model.getAdaptations()) {
            if (p instanceof TypeAdaptation) {
                typeAdaptationFound = true;
            }
        }
        if (typeAdaptationFound) {
            System.out.println("Type adaptation detected -> full firmware update needed !");
            //Step : Type Bundle preparation step
            if (bcontext != null) {
                System.out.println("Install Type definition");
                TypeBundleBootstrap.bootstrapTypeBundle(model, bcontext);
            } else {
                System.out.println("Warning no OSGi runtime available");
            }
            //Step : Generate firmware code to output path
            KevoreeCGenerator generator = new KevoreeCGenerator();
            generator.generate(model, nodeName, outputPath, bcontext);

//STEP 3 : Deploy by commnication channel
            progress.beginTask("Prepare compilation", 40);
            ArduinoCompilation arduinoCompilation = new ArduinoCompilation();
            ArduinoLink arduinoLink = new ArduinoLink();
            ArduinoArchive arduinoArchive = new ArduinoArchive();
            ArduinoPostCompilation arduinoPostCompilation = new ArduinoPostCompilation();
            ArduinoDeploy arduinoDeploy = new ArduinoDeploy();
            arduinoCompilation.prepareCommands();
            arduinoLink.prepareCommands();
            arduinoArchive.prepareCommands();
            arduinoPostCompilation.prepareCommands();
            arduinoDeploy.prepareCommands();
            ArduinoBuildEnvironment arduinoBuildEnvironment = ArduinoBuildEnvironment.getInstance();
            Target target = arduinoBuildEnvironment.getDefaultTargetList().getTarget(getDictionary().get("boardTypeName").toString());
            progress.endTask();
            try {
                progress.beginTask("Prepare sketch", 50);
                Sketch sketch = new Sketch(newdir);
                sketch.preprocess(target);
                progress.endTask();
                Core core = CodeManager.getInstance().getCore(target);
                arduinoCompilation.compileCore(sketch, target, core);
                progress.beginTask("Library processing", 60);
                for (org.wayoda.ang.libraries.Library library : sketch.getLibraries()) {
                    System.out.println("----Lib " + library.getName());

                    arduinoCompilation.compileLibrary(sketch, target, library, core);
                }
                progress.endTask();

                progress.beginTask("Firmware compilation", 70);
                arduinoCompilation.compileSketch(sketch, target, core);
                arduinoArchive.archiveSketch(sketch, target);
                progress.endTask();

                progress.beginTask("Firmware linkage", 80);
                arduinoLink.linkSketch(sketch, target);
                arduinoPostCompilation.postCompileSketch(sketch, target);
                progress.endTask();

                String boardName = "";
                if (getDictionary().get("boardPortName") != null) {
                    boardName = getDictionary().get("boardPortName").toString();
                }

                if (boardName == null || boardName.equals("")) {
                    boardName = GuiAskForComPort.askPORT();
                }

                System.out.println("boardPortName=" + boardName);

                progress.beginTask("Upload to arduino board", 90);
                arduinoDeploy.uploadSketch(sketch, target, boardName);
                progress.endTask();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ArduinoNode.class.getName()).log(Level.SEVERE, null, ex);
                progress.failTask();
            }

        } else {
            System.out.println("incremental update availble -> try to generate KevScript !");


        }






        return true;
    }
}
