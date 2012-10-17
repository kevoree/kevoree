/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
*
*
*
*                          THIS IS A GENERATED FILE, DO NOT EDIT
*
*
*
*
*
*
**/
package org.kevoree.library.nativeN.Pwm;

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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
/**
 * this is a generated file, DO NOT EDIT
 * User: jed
 * Date: 08/10/12
 * Time: 08:10
 */
@DictionaryType({
        @DictionaryAttribute(name = "ipc_key", optional = false),
        @DictionaryAttribute(name = "portEvents_tcp", optional = false),
        @DictionaryAttribute(name = "portEvents_udp", optional = false)
})
@ComponentType
@Provides({ @ProvidedPort(name = "in_pwm", type = PortType.MESSAGE,theadStrategy = ThreadStrategy.NONE)
})

@Library(name = "Native")
public class Pwm extends AbstractComponentType {

    private  int ipc_key = 251102;
    private int portEvents_tcp = 9865;
    private NativeManager nativeManager = null;
    private boolean started = false;


    @Start
    public void start () {
        try
        {
            ipc_key  = Integer.parseInt(getDictionary().get("ipc_key").toString());
            portEvents_tcp =       Integer.parseInt(getDictionary().get("portEvents_tcp").toString());

            ArrayList<String> repos = new ArrayList<String>();
            for(Repository repo :  getModelService().getLastModel().getRepositoriesForJ())
            {
                repos.add(repo.getUrl());
            }
            // loading model from jar
            ContainerRoot model = KevoreeXmiHelper.loadStream(getClass().getResourceAsStream("/KEV-INF/lib.kev"));

            File binary =    getBootStrapperService().resolveArtifact("org.kevoree.library.nativeN.pwm_native"+getOs(), "org.kevoree.library.nativeN", "1.8.9-SNAPSHOT", "uexe", repos);

            if(!binary.canExecute())
            {
                binary.setExecutable(true);
            }

            nativeManager = new NativeManager(ipc_key,portEvents_tcp,"Pwm",binary.getPath(),model);

            nativeManager.addEventListener(new NativeListenerPorts() {
                @Override
                public void disptach(NativeEventPort event, String port_name, String msg)
                {
                    MessagePort port = (MessagePort) getPortByName(port_name);
                    port.process(msg);
                }
            });

            started = nativeManager.start();

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
    public void update () {
        ipc_key  = Integer.parseInt(getDictionary().get("ipc_key").toString());
        portEvents_tcp =       Integer.parseInt(getDictionary().get("portEvents_tcp").toString());
        nativeManager.update();
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

        @Port(name = "in_pwm")
    public void in_pwm(Object o)
    {
        if(nativeManager != null)
        {
            nativeManager.push("in_pwm",o.toString());
            
        }   else 
        {
            System.err.println("Error processing message");
        }
    }
}