package org.kevoree.library.arduinoNodeType;


import eu.powet.fota.Fota;
import eu.powet.fota.api.FotaEventListener;
import eu.powet.fota.events.FotaEvent;
import eu.powet.fota.utils.Board;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.checker.CheckerViolation;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.extra.kserial.KevoreeSharedCom;
import org.kevoree.kompare.JavaSePrimitive;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.arduinoNodeType.generator.KevoreeCGenerator;
import org.kevoree.library.arduinoNodeType.util.ArduinoResourceHelper;
import org.kevoree.library.arduinoNodeType.utils.ComSender;
import org.kevoree.tools.marShell.ast.Script;
import org.kevoree.tools.marShellTransform.AdaptationModelWrapper;
import org.kevoree.tools.marShellTransform.KevScriptWrapper;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wayoda.ang.libraries.CodeManager;
import org.wayoda.ang.libraries.Core;
import org.wayoda.ang.project.ArduinoBuildEnvironment;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 13/07/12
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
@org.kevoree.annotation.DictionaryType({
        @org.kevoree.annotation.DictionaryAttribute(name = "boardTypeName", defaultValue = "uno", optional = true, vals = {"uno"}),
        @org.kevoree.annotation.DictionaryAttribute(name = "incremental", defaultValue = "true", optional = true, vals = {"true", "false"}),
        @org.kevoree.annotation.DictionaryAttribute(name = "pmem", defaultValue = "eeprom", optional = true, vals = {"eeprom", "sd"}),
        @org.kevoree.annotation.DictionaryAttribute(name = "psize", defaultValue = "MAX", optional = true)  ,
        @org.kevoree.annotation.DictionaryAttribute(name = "timeout", defaultValue = "25", optional = false)
})
@org.kevoree.annotation.NodeType
public class ArduinoWirelessNode extends ArduinoNode
{
    private static final Logger logger = LoggerFactory.getLogger(ArduinoWirelessNode.class);
    public String outputPath = "";
    private static boolean finished = false;
    private int timeout = 120;
    private  Sketch sketch;
    private String boardName = "";
    private Target target=null;
    @org.kevoree.annotation.Start
    public void startNode() {
        super.startNode();
    }

    @org.kevoree.annotation.Stop
    public void stopNode() {
        super.stopNode();
    }


    public boolean deploy(AdaptationModel modelIn, String nodeName, String boardPortName)
    {

        boolean typeAdaptationFound = false;
        ContainerRoot rootModel = null;
        for (AdaptationPrimitive p : modelIn.getAdaptationsForJ()) {
            Boolean addType = p.getPrimitiveType().getName().equals(JavaSePrimitive.AddType());
            Boolean removeType = p.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveType());
            Boolean updateType = p.getPrimitiveType().getName().equals(JavaSePrimitive.UpdateType());
            if (addType || removeType || updateType)
            {
                typeAdaptationFound = true;
            }
            rootModel = tempRoot;
        }

        if (typeAdaptationFound || forceUpdate)
        {
            java.util.List<CheckerViolation> result = localChecker.check(rootModel);
            if (result.size() > 0) {
                for (CheckerViolation cv : result) {
                    logger.error("Checker Error = " + cv.getMessage());
                }
                return false;
            }

            KevoreeKompareBean kompare = new KevoreeKompareBean();

            ModelCloner cloner = new ModelCloner();
            ContainerRoot lastVersionModel = cloner.clone(rootModel);
            for (ContainerNode node : lastVersionModel.getNodesForJ()) {
                node.removeAllComponents();
                node.removeAllHosts();
            }
            lastVersionModel.removeAllMBindings();
            lastVersionModel.removeAllGroups();

            AdaptationModel model = kompare.kompare(lastVersionModel, rootModel, nodeName);

            //Must compute a dif from scratch model

            logger.debug("Type adaptation detected -> full firmware update needed !");

            logger.debug("Install Type definitions");
            TypeBundleBootstrap.bootstrapTypeBundle(model, this);

            KevoreeCGenerator generator = new KevoreeCGenerator();


            PMemory pm = PMemory.EEPROM;
            if (this.getDictionary().get("pmem") != null) {
                String s = this.getDictionary().get("pmem").toString();
                if (s.toLowerCase().equals("eeprom")) {
                    pm = PMemory.EEPROM;
                }
                if (s.toLowerCase().equals("sd")) {
                    pm = PMemory.SD;
                }
            }

            String psize = "";
            if (this.getDictionary().get("psize") != null && this.getDictionary().get("psize") != "MAX") {
                psize = this.getDictionary().get("psize").toString();
            }
            try{
                newdir.mkdirs();
                outputPath = newdir.getAbsolutePath();
                generator.generate(model, nodeName, outputPath, getDictionary().get("boardTypeName").toString(), pm, psize, this);

            }catch (Exception e)
            {
                System.out.println(" error = "+e);
            }
            try{
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
                target = arduinoBuildEnvironment.getDefaultTargetList().getTarget(getDictionary().get("boardTypeName").toString());
                // progress.endTask();

                //  progress.beginTask("Prepare sketch", 50);
                sketch= new Sketch(newdir);
                sketch.preprocess(target);
                //   progress.endTask();
                Core core = CodeManager.getInstance().getCore(target);
                //System.err.println("core :=> " + core + " -> " + target.getCore());
                arduinoCompilation.compileCore(sketch, target, core);
                // progress.beginTask("Library processing", 60);

                for (org.wayoda.ang.libraries.Library library : sketch.getLibraries()) {
                    logger.debug("----Lib " + library.getName());
                    arduinoCompilation.compileLibrary(sketch, target, library, core);
                }
                //progress.endTask();

                // progress.beginTask("Firmware compilation", 70);
                arduinoCompilation.compileSketch(sketch, target, core, generator.context().getGenerator());
                arduinoArchive.archiveSketch(sketch, target);
                //progress.endTask();

                //progress.beginTask("Firmware linkage", 80);
                arduinoLink.linkSketch(sketch, target);
                arduinoPostCompilation.postCompileSketch(sketch, target);
                //   progress.endTask();


                if (boardPortName != null && !boardPortName.equals("")) {
                    boardName = boardPortName;
                }

                if (boardName == null || boardName.equals("")) {
                    boardName = GuiAskForComPort.askPORT();
                }

                logger.debug("boardPortName=" + boardName);


                KevoreeSharedCom.lockPort(boardName);

                Thread flash = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            final Fota fota = new Fota(boardName, Board.ATMEGA328);


                            if (getDictionary().get("timeout") != null)
                            {
                                timeout = Integer.parseInt(getDictionary().get("timeout").toString());
                            }


                            fota.addEventListener(new FotaEventListener()
                            {
                                // @Override
                                public void progressEvent(FotaEvent evt) {
                                    System.out.println(" Uploaded " + evt.getSize_uploaded()+"/"+evt.getProgram_size() + " octets");
                                    timeout += 1;
                                }

                                @Override
                                public void completedEvent(FotaEvent evt) {
                                    System.out.println("Transmission completed successfully <" + evt.getProgram_size() + " octets "+evt.getDuree()+" secondes >");
                                    finished = true;

                                }
                            });

                            fota.upload(sketch.getPath(target));

                            while(finished == false && fota.getDuree() < timeout)
                            {
                                Thread.sleep(1000);
                            }
                        }catch(Exception e)
                        {
                          e.printStackTrace();
                        }
                    }
                });

                flash.start();

                flash.join();


                KevoreeSharedCom.unlockPort(boardName);

            }catch (Exception e)
            {
                System.out.println(" error = "+e);
            }


        } else {
            logger.debug("incremental update available -> try to generate KevScript !");
            Script baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(modelIn));
            String resultScript = KevScriptWrapper.generateKevScriptCompressed(baseScript, this.getNodeName());
            logger.debug(resultScript);
            String boardName = "";
            if (boardPortName != null && !boardPortName.equals("")) {
                boardName = boardPortName;
            }
            if (boardName == null || boardName.equals("")) {
                boardName = GuiAskForComPort.askPORT();
            }
            try {
                ComSender.ping(boardName);
                ComSender.send(resultScript, boardName);
            } catch (Exception e) {
//                e.printStackTrace();
                logger.error("", e);
            }


        }

        logger.info("Arduino finished");
        return true;

    }
}
