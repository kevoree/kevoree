/*
*
*
*
*                          THIS IS A GENERATED FILE, DO NOT EDIT
*
*
**/

package org.kevoree.library.nativeN.HelloWorld;

import org.kevoree.ContainerRoot;
import org.kevoree.Repository;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.MessagePort;
import org.kevoree.tools.aether.framework.AetherUtil;
import org.kevoree.tools.nativeN.NativeHandlerException;
import org.kevoree.tools.nativeN.NativeManager;
import org.kevoree.tools.nativeN.api.NativeEventPort;
import org.kevoree.tools.nativeN.api.NativeListenerPorts;
import java.util.Random;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
/**
 * this is a generated file, DO NOT EDIT
 * User: jed
 * Date: 08/10/12
 * Time: 08:10
 */

@ComponentType
@Provides({ @ProvidedPort(name = "input_port", type = PortType.MESSAGE,theadStrategy = ThreadStrategy.NONE)
})@Requires({ @RequiredPort(name = "output_port", type = PortType.MESSAGE,optional = true,theadStrategy = ThreadStrategy.NONE)
})@DictionaryType({ @DictionaryAttribute(name = "myParam", defaultValue = "", optional = true),
@DictionaryAttribute(name = "myParam2", defaultValue = "", optional = true)
})
@Library(name = "Native")
public class HelloWorld extends AbstractComponentType {

    private  int ipc_key = 251102;
    private NativeManager nativeManager = null;
    private boolean started = false;
    private final int range_min = 2000;
    private final int range_max = 65535;

    @Start
    public void start () {
        try
        {
            // todo check if not used
            ipc_key  = gerRandom();
            ArrayList<String> repos = new ArrayList<String>();
            for(Repository repo :  getModelService().getLastModel().getRepositoriesForJ())
            {
                repos.add(repo.getUrl());
            }
            // loading model from jar
            ContainerRoot model = KevoreeXmiHelper.loadStream(getClass().getResourceAsStream("/KEV-INF/lib.kev"));
            File binary =    getBootStrapperService().resolveArtifact("org.kevoree.library.nativeN.HelloWorld-native"+getOs(), "org.kevoree.library.nativeN", "1.8.9-SNAPSHOT", "uexe", repos);
            if(binary != null)
            {

                if(!binary.canExecute())
                {
                    binary.setExecutable(true);
                }

                nativeManager = new NativeManager(ipc_key,binary.getPath(),model);

                nativeManager.addEventListener(new NativeListenerPorts() {
                    @Override
                    public void disptach(NativeEventPort event, String port_name, String msg)
                    {
                        MessagePort port = (MessagePort) getPortByName(port_name);
                        port.process(msg);
                    }
                });

                started = nativeManager.start();

                nativeManager.setDico("myParam",getDictionary().get("myParam").toString()); 
nativeManager.setDico("myParam2",getDictionary().get("myParam2").toString()); 


             } else
             {
                 System.err.println("The binary org.kevoree.library.nativeN.HelloWorld-native for the architecture "+getOs()+" is not found");
             }

        } catch (NativeHandlerException e) {
            e.printStackTrace();
        }
    }

    @Stop
    public void stop () {
        if(started)
        {
            try
            {
                if(nativeManager !=null)
                    nativeManager.stop();
            } catch (NativeHandlerException e) {
                e.printStackTrace();
            }
        }
    }

    @Update
    public void update ()
    {
       if(nativeManager !=null)
       {
            nativeManager.setDico("myParam",getDictionary().get("myParam").toString()); 
nativeManager.setDico("myParam2",getDictionary().get("myParam2").toString()); 

            nativeManager.update();
       }
    }

    public boolean isArm() {
        String os = System.getProperty("os.arch").toLowerCase();
        return (os.contains("arm"));
    }

    public boolean is64() {
        String os = System.getProperty("os.arch").toLowerCase();
        return (os.contains("64"));
    }

    public  String getOs() {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            if(isArm())
            {
                return "-arm";
            }
            if (is64()) {
                return "-nix64";
            } else {
                return "-nix32";
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "-osx";
        }
        return null;
    }

  public int gerRandom(){
            return range_min + new Random().nextInt(range_max - range_min);
  }

        @Port(name = "input_port")
    public void input_port(Object o)
    {
        if(nativeManager != null)
        {
            nativeManager.push("input_port",o.toString());
            
        }   else 
        {
            System.err.println("Error processing message");
        }
    }
}