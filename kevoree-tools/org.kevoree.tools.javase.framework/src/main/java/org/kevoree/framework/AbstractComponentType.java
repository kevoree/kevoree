/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.framework;

import org.kevoree.ComponentInstance;
import org.kevoree.api.Bootstraper;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.framework.port.KevoreeRequiredPort;

import java.util.HashMap;

/**
 * @author ffouquet
 */
public class AbstractComponentType implements ComponentType {

    private KevScriptEngineFactory kevScriptEngineFactory = null;

    public KevScriptEngineFactory getKevScriptEngineFactory() {
        return kevScriptEngineFactory;
    }

    public void setKevScriptEngineFactory(KevScriptEngineFactory kf) {
        kevScriptEngineFactory = kf;
    }


    private ModelHandlerServiceProxy modelServiceProxy = new ModelHandlerServiceProxy();

    public void setModelService(KevoreeModelHandlerService ms) {
        modelServiceProxy.setProxy(ms);
    }

    public KevoreeModelHandlerService getModelService() {
        return modelServiceProxy;
    }

    private HashMap<String, Object> hostedPorts = new HashMap<String, Object>();
    private HashMap<String, Object> neededPorts = new HashMap<String, Object>();
    private HashMap<String, Object> dictionary = new HashMap<String, Object>();

    @Override
    public HashMap<String, Object> getDictionary() {
        return this.dictionary;
    }

    private String nodeName = "";

    @Override
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String pnodeName) {
        nodeName = pnodeName;
    }

    private String name = "";

    @Override
    public String getName() {
        return name;
    }

    public void setName(String pname) {
        name = pname;
    }


    @Override
    public void setDictionary(HashMap<String, Object> dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void setHostedPorts(HashMap<String, Object> ports) {
        this.hostedPorts = ports;
        for (Object p : this.hostedPorts.values()) {
            ((AbstractPort) p).setComponent(this);
        }
    }

    @Override
    public HashMap<String, Object> getNeededPorts() {
        return neededPorts;
    }

    @Override
    public void setNeededPorts(HashMap<String, Object> neededPorts) {
        this.neededPorts = neededPorts;
    }

    public Object getPortByName(String portName) {
        Object port = null;
        if (this.getHostedPorts().containsKey(portName)) {
            port = this.getHostedPorts().get(portName);
        }
        if (this.getNeededPorts().containsKey(portName)) {
            port = this.getNeededPorts().get(portName);
        }
        return port;
    }

    @Override
    public <T> T getPortByName(String name, Class<T> type) {
        Object retp = getPortByName(name);
        if (retp != null) {
            return (T) retp;
        } else {
            return null;
        }
    }

    @Override
    public HashMap<String, Object> getHostedPorts() {
        return this.hostedPorts;
    }

    @Override
    public Boolean isPortBinded(String portName) {
        Object port = null;
        if (this.getNeededPorts().containsKey(portName)) {
            port = this.getNeededPorts().get(portName);
        }
        if (port != null) {
            KevoreeRequiredPort rport = (KevoreeRequiredPort) port;
            return rport.getIsBound();
        } else {
            return false;
        }
    }

    /**
     * Allow to find the corresponding element into the model
     *
     * @return the component instance corresponding to this
     */
    public ComponentInstance getModelElement() {
        return KevoreeElementHelper.getComponentElement(this.getName(), this.getNodeName(), this.getModelService().getLastModel()).get();
    }


    private Bootstraper bootstrapService = null;

    public void setBootStrapperService(Bootstraper brs) {
        bootstrapService = brs;
    }

    public Bootstraper getBootStrapperService() {
        return bootstrapService;
    }


}
