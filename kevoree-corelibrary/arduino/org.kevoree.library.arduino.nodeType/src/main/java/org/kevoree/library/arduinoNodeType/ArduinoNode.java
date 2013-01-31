package org.kevoree.library.arduinoNodeType;


import org.kevoree.*;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.service.core.checker.CheckerViolation;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.extra.kserial.KevoreeSharedCom;
import org.kevoree.extra.kserial.Utils.KHelpers;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.DefaultKevoreeFactory;
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
import org.wayoda.ang.project.TargetDirectoryService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@org.kevoree.annotation.NodeType
@org.kevoree.annotation.Library(name = "Arduino")
@org.kevoree.annotation.DictionaryType({
        @org.kevoree.annotation.DictionaryAttribute(name = "boardTypeName", defaultValue = "uno", optional = true, vals = {"uno", "atmega328", "mega2560"}),
        @org.kevoree.annotation.DictionaryAttribute(name = "incremental", defaultValue = "true", optional = true, vals = {"true", "false"}),
        @org.kevoree.annotation.DictionaryAttribute(name = "pmem", defaultValue = "eeprom", optional = true, vals = {"eeprom", "sd"}),
        @org.kevoree.annotation.DictionaryAttribute(name = "psize", defaultValue = "MAX", optional = true)
})
@org.kevoree.annotation.PrimitiveCommands(values = {"StartThirdParty", "UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty"}, value = {})

public class ArduinoNode extends AbstractNodeType {

    private static final Logger logger = LoggerFactory.getLogger(ArduinoNode.class);
    public static final int baudrate = 19200;
    protected File newdir = null;
    protected ArduinoChecker localChecker = null;
    protected Boolean forceUpdate = false;
    protected String outputPath = "";
    protected ContainerRoot tempRoot = null;
    protected  ArduinoDeploy arduinoDeploy=null;
    protected  Sketch sketch=null;
    protected Target target= null;

    public void setForceUpdate(Boolean f) {
        forceUpdate = f;
    }

    @org.kevoree.annotation.Start
    public void startNode() {
        localChecker = new ArduinoChecker(getNodeName());
        ArduinoResourceHelper.setBs(getBootStrapperService());
    }

    @org.kevoree.annotation.Stop
    public void stopNode() {
        ArduinoResourceHelper.setBs(null);
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return null; //NOT TO BE USED WITH KEVOREE CORE
    }

    @Override
    public org.kevoree.api.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return null;//NOT TO BE USED WITH KEVOREE CORE
    }

    //@Override
    public  void push(final String targetNodeName, final ContainerRoot root, String boardPortName) throws IOException {
        try
        {
            if(boardPortName.equals("*"))
            {
                if(KHelpers.getPortIdentifiers().size() > 0)
                {
                    KHelpers.getPortIdentifiers().size();
                    boardPortName =  KHelpers.getPortIdentifiers().get(0);
                } else
                {
                    logger.error("Sorry, we have not detected on your machine arduino ");
                }
            }

            KevoreeKompareBean kompare = new KevoreeKompareBean();
            newdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "arduinoGenerated" + targetNodeName);
            newdir.delete();
            newdir.mkdirs();

            KevoreeFactory factory = new DefaultKevoreeFactory();

            ContainerRoot lastVersionModel = factory.createContainerRoot();

            int lastVersion = 0;
            //Try to find previous version
            if (getDictionary().get("incremental") != null && getDictionary().get("incremental").equals("true"))
            {

                logger.info("Incremental search");
                lastVersionModel = ArduinoModelGetHelper.getCurrentModel(root, targetNodeName, boardPortName);

            }
            else
            {
                //CLEAR PREVIOUS SAVED MODEL
                for (File f : newdir.listFiles()) {
                    if (f.getName().endsWith(".kev")) {
                        f.delete();
                    }
                }
            }


            if (lastVersionModel == null || lastVersionModel.getNodes().size() == 0) {
                logger.info("No Previous Model , Init one from targetModel");
                ModelCloner cloner = new ModelCloner();
                lastVersionModel = cloner.clone(root);
                for (ContainerNode node : lastVersionModel.getNodes()) {
                    node.removeAllComponents();
                    node.removeAllHosts();
                }
                lastVersionModel.removeAllMBindings();
                lastVersionModel.removeAllGroups();
            }
            tempRoot = root;

            ModelCloner cc = new ModelCloner();
            ContainerRoot cloned = cc.clone(root);
            cloned.removeAllGroups();


            AdaptationModel kompareModel = kompare.kompare(lastVersionModel, cloned, targetNodeName);


            if (kompareModel.getAdaptations().size() > 0) {

                File newdirTarget = new File(newdir.getAbsolutePath() + File.separator + "target");
                org.kevoree.library.arduinoNodeType.FileHelper.createAndCleanDirectory(newdirTarget);
                TargetDirectoryService.rootPath = newdirTarget.getAbsolutePath();
                outputPath = newdir.getAbsolutePath();
                logger.debug("outDir=" + outputPath);

                try {

                    if (deploy(kompareModel, targetNodeName, boardPortName)) {
                        //     progress.endTask();
                    } else {
                        logger.error("Error appears when we compute the firmware update");
                    }
                } catch (Exception e) {
                    // progress.failTask();

                    logger.error("Error appears when we compute the firmware update", e);
                }

                KevoreeXmiHelper.$instance.save(newdir.getAbsolutePath() + File.separator + targetNodeName + "_" + (lastVersion + 1) + ".kev", root);
            }

        }catch (Exception e)
        {
            logger.error("",e);
        }
    }



    public boolean deploy(AdaptationModel modelIn, String nodeName, String boardPortName) {

        if (needAdaptation(modelIn) || forceUpdate)
        {
            if(checker() == false){ return false;}
            // compile
            compile(tempRoot, nodeName, boardPortName);
            // lock port
            KevoreeSharedCom.lockPort(boardPortName);
            // avrdude
            arduinoDeploy.uploadSketch(sketch, target, boardPortName);
            // unlock
            KevoreeSharedCom.unlockPort(boardPortName);
        } else
        {
            incremental(modelIn, nodeName, boardPortName);
        }
        return true;
    }



    public boolean compile(ContainerRoot rootModel, String nodeName, String boardPortName){

        KevoreeKompareBean kompare = new KevoreeKompareBean();

        ModelCloner cloner = new ModelCloner();
        ContainerRoot lastVersionModel = cloner.clone(rootModel);
        for (ContainerNode node : lastVersionModel.getNodes()) {
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

        generator.generate(model, nodeName, outputPath, getDictionary().get("boardTypeName").toString(), pm, psize, this);

        ArduinoCompilation arduinoCompilation = new ArduinoCompilation();
        ArduinoLink arduinoLink = new ArduinoLink();
        ArduinoArchive arduinoArchive = new ArduinoArchive();
        ArduinoPostCompilation arduinoPostCompilation = new ArduinoPostCompilation();
        arduinoDeploy = new ArduinoDeploy();
        arduinoCompilation.prepareCommands();
        arduinoLink.prepareCommands();
        arduinoArchive.prepareCommands();
        arduinoPostCompilation.prepareCommands();
        arduinoDeploy.prepareCommands();
        ArduinoBuildEnvironment arduinoBuildEnvironment = ArduinoBuildEnvironment.getInstance();
        target = arduinoBuildEnvironment.getDefaultTargetList().getTarget(getDictionary().get("boardTypeName").toString());
        try
        {

            sketch = new Sketch(newdir);
            sketch.preprocess(target);

            Core core = CodeManager.getInstance().getCore(target);
            logger.debug("core :=> " + core + " -> " + target.getCore());
            arduinoCompilation.compileCore(sketch, target, core);

            for (org.wayoda.ang.libraries.Library library : sketch.getLibraries()) {
                logger.debug("----Lib " + library.getName());
                arduinoCompilation.compileLibrary(sketch, target, library, core);
            }

            arduinoCompilation.compileSketch(sketch, target, core, generator.context().getGenerator());
            arduinoArchive.archiveSketch(sketch, target);
            arduinoLink.linkSketch(sketch, target);
            arduinoPostCompilation.postCompileSketch(sketch, target);

            String boardName = "";
            if (boardPortName != null && !boardPortName.equals("")) {
                boardName = boardPortName;
            }

            if (boardName == null || boardName.equals("")) {
                boardName = GuiAskForComPort.askPORT();
            }

            logger.debug("boardPortName=" + boardName);

        } catch (FileNotFoundException ex) {
            logger.error("", ex);
            return false;
        }
        return true;
    }

    public  boolean needAdaptation(AdaptationModel modelIn){
        boolean typeAdaptationFound = false;
        for (AdaptationPrimitive p : modelIn.getAdaptations()) {
            Boolean addType = p.getPrimitiveType().getName().equals(JavaSePrimitive.AddType());
            Boolean removeType = p.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveType());
            Boolean updateType = p.getPrimitiveType().getName().equals(JavaSePrimitive.UpdateType());
            if (addType || removeType || updateType) {
                typeAdaptationFound = true;
            }
        }
        return typeAdaptationFound;
    }

    public  boolean  checker()
    {
        // checker
        java.util.List<CheckerViolation> result = localChecker.check(tempRoot);
        if (result.size() > 0) {
            for (CheckerViolation cv : result) {
                logger.error("Checker Error = " + cv.getMessage());
            }
            return false;
        }
        return true;
    }



    public void incremental(AdaptationModel modelIn,String nodeName, String boardPortName){

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
            logger.error("", e);
        }

    }

}
