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

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 12/04/11
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeRemoteBean {

    private Component component;
    private Application provisioning = null;

    public KevoreeRemoteBean() {

        Integer port = NetworkUtility.findNextAvailblePort(8000, 60000);
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void stop() {
        component.getDefaultHost().detach(ModelHandlerResource.class);
        if(provisioning !=null){
            component.getDefaultHost().detach(provisioning);
        }


        try {
            component.stop();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
