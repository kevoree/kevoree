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
package org.kevoree.tools.nativeN;

import org.kevoree.ComponentType;
import org.kevoree.ContainerRoot;
import org.kevoree.PortTypeRef;
import org.kevoree.TypeDefinition;
import org.kevoree.tools.nativeN.api.INativeManager;
import org.kevoree.tools.nativeN.api.NativeEventPort;
import org.kevoree.tools.nativeN.api.NativeListenerPorts;

import javax.swing.event.EventListenerList;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/08/12
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public class NativeManager implements INativeManager {

    protected EventListenerList listenerList = new EventListenerList();
    private NativeJNI nativeJNI;
    private String path_uexe;
    private int key;

    private LinkedHashMap<String,Integer> inputs_ports =new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String,Integer> ouputs_ports = new LinkedHashMap<String, Integer>();

    public NativeManager(final int key,final String componentName,final String path_uexe ,ContainerRoot model) throws NativeHandlerException
    {
        this.key = key;
        this.path_uexe =path_uexe;
        nativeJNI = new NativeJNI(this);
        nativeJNI.configureCL();
        nativeJNI.init(key);
        for(TypeDefinition type :  model.getTypeDefinitionsForJ())
        {
            if(type instanceof ComponentType)
            {
                ComponentType c = (ComponentType)type;
                if(c.getName().equals(componentName))
                {
                    for(PortTypeRef portP :  c.getProvidedForJ())
                    {
                        inputs_ports.put(portP.getName(), nativeJNI.create_input(key, portP.getName()));
                    }
                    for(PortTypeRef portR :  c.getRequiredForJ())
                    {
                        inputs_ports.put(portR.getName(), nativeJNI.create_output(key, portR.getName()));
                    }
                    break;
                }
            }
        }
        nativeJNI.register();
    }


    public  boolean start() throws NativeHandlerException {

        if(nativeJNI.start(key, path_uexe) < 0)
        {
          return false;
        }
        //todo check started  remove sleep
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        return true;
    }

    public boolean stop() throws NativeHandlerException
    {
        nativeJNI.stop(key);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return  true;
    }


    public void addEventListener (NativeListenerPorts listener) {
        listenerList.add(NativeListenerPorts.class, listener);
    }

    public void removeEventListener (NativeListenerPorts listener) {
        listenerList.remove(NativeListenerPorts.class, listener);
    }


    public void fireEvent(NativeEventPort evt,String queue,String msg)
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2)
        {
            if (evt instanceof NativeEventPort)
            {
                ((NativeListenerPorts) listeners[i + 1]).disptach(evt,queue,msg);
            }
        }
    }
    public boolean update()
    {
        if(nativeJNI.update(key) != 0){
            return  false;
        }
        return  true;
    }

    public void setInputs_ports(LinkedHashMap<String, Integer> inputs_ports) {
        this.inputs_ports = inputs_ports;
    }

    public void setOuputs_ports(LinkedHashMap<String, Integer> ouputs_ports) {
        this.ouputs_ports = ouputs_ports;
    }



    public  boolean setDico(String name,String value){
        if(nativeJNI.setDico(key,name,value) != 0){
            return  false;
        }
        return  true;
    }
    public boolean push(String port, String msg)
    {
        if(nativeJNI.putPort(key, inputs_ports.get(port), msg) != 0){
            return  false;
        }
        return  true;
    }
}


