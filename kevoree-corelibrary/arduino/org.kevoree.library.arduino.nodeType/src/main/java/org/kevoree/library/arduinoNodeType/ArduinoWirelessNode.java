package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.Update;
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler;
import org.kevoree.extra.kserial.KevoreeSharedCom;
import org.kevoree.fota.Fota;
import org.kevoree.fota.Nativelib;
import org.kevoree.fota.api.FotaEventListener;
import org.kevoree.fota.events.FotaEvent;
import org.kevoree.fota.utils.Board;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoreeAdaptation.AdaptationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        @org.kevoree.annotation.DictionaryAttribute(name = "timeout", defaultValue = "60", optional = false)
})
@org.kevoree.annotation.NodeType
public class ArduinoWirelessNode extends ArduinoNode
{
    private static final Logger logger = LoggerFactory.getLogger(ArduinoWirelessNode.class);
    public String outputPath = "";
    private boolean finished = false;
    private int timeout = 10;
    private String boardName = "";

    @org.kevoree.annotation.Start
    public void startNode()
    {
        super.startNode();
        dico();
    }

    @org.kevoree.annotation.Stop
    public void stopNode() {
        super.stopNode();
    }

    @Update
    public void update()
    {
        dico();
    }

    public void dico(){
        if (getDictionary().get("timeout") != null)
        {
            timeout = Integer.parseInt(getDictionary().get("timeout").toString());
        }
    }


    public  void push(final String targetNodeName, final ContainerRoot root, String boardPortName) throws IOException {
        super.push(targetNodeName,root,boardPortName);
    }

    public boolean deploy(AdaptationModel modelIn, String nodeName, final String boardPortName)
    {


        if (needAdaptation(modelIn) || forceUpdate)
        {
            if(checker() == false){ return false;}

            KevoreeSharedCom.killAll();

            // compile
            compile(tempRoot, nodeName, boardPortName);
            try
            {
                KevoreeSharedCom.lockPort(boardPortName);

                Thread flash = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Fota fota=null;

                        try
                        {


                         KevoreeJarClassLoader kcl = (KevoreeJarClassLoader) this.getClass().getClassLoader();

                            kcl.addNativeMapping("native",Nativelib.configureCL());

                            System.loadLibrary("native");


                            fota = new Fota(boardPortName, Board.ATMEGA328);

                            fota.addEventListener(new FotaEventListener() {
                                // @Override
                                public void progressEvent(FotaEvent evt) {
                                    logger.info(" Uploaded " + evt.getSize_uploaded() + "/" + evt.getProgram_size() + " octets");
                                    timeout += 1;
                                }

                                @Override
                                public void completedEvent(FotaEvent evt) {
                                    logger.info("Transmission completed successfully <" + evt.getProgram_size() + " octets " + evt.getDuree() + " secondes >");
                                    finished = true;

                                }
                            });

                            String path_hex =sketch.getPath(target);
                            logger.debug("Fota with "+path_hex);
                            fota.upload(path_hex);
                            dico();
                            fota.waitingUpload(timeout);

                            fota = null;

                        }catch(Exception e)
                        {
                            System.out.println(e);
                            logger.error("Fota ",e);
                        }
                    }
                });

                flash.start();

                flash.join();

            }  catch (Exception e){
                  logger.error("Fota ",e);
            }  finally
            {
                KevoreeSharedCom.unlockPort(boardPortName);
            }

        } else
        {
            incremental(modelIn,nodeName,boardPortName);
        }

        return true;

    }
}
