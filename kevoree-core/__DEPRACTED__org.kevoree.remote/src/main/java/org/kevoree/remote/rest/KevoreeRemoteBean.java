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
package org.kevoree.remote.rest;

import org.kevoree.remote.NetworkUtility;
import org.kevoree.remote.fileserver.RestFileServerApplication;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KevoreeRemoteBean {

    private Component component;
    private Application provisioning = null;

    Integer port = 8000;
    private Logger logger = LoggerFactory.getLogger(KevoreeRemoteBean.class);

    public Integer getPort(){
        return port;
    }

    public KevoreeRemoteBean(String portParam) {

        try {
            Integer tmpPort = Integer.parseInt(portParam);
            port = tmpPort;
        } catch (Exception e) {
        }
        port = NetworkUtility.findNextAvailblePort(port, 60000);

        System.out.println("Kevoree Remote Port => " + port);

        component = new Component();

        component.getServers().add(Protocol.HTTP, port);
        component.getClients().add(Protocol.FILE);
        component.getContext().getParameters().add("timeToLive", "0");

        component.getDefaultHost().attach("/model/current", ModelHandlerResource.class);

        if (System.getProperty("org.kevoree.remote.provisioning") != null) {
            provisioning = new RestFileServerApplication(System.getProperty("org.kevoree.remote.provisioning"));
            component.getDefaultHost().attach("/provisioning", provisioning);
            System.out.println("Provisioning server started => /provisioning");
        } //else {
        //component.getDefaultHost.attachDefault(classOf[ErrorResource])
        // }


    }

    public void start() {
        try {
            component.start();
            Handler.initHost(component.getDefaultHost());
        } catch (Exception e) {
            logger.error("Restlet Start Error", e);
        }
    }

    public void stop() {
        component.getDefaultHost().detach(ModelHandlerResource.class);
        if (provisioning != null) {
            component.getDefaultHost().detach(provisioning);
        }


        try {
            component.stop();
        } catch (Exception e) {
            logger.error("Restlet Stop Error", e);
        }
    }


}
