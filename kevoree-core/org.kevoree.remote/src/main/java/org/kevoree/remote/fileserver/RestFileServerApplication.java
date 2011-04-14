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
package org.kevoree.remote.fileserver;


import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;

public class RestFileServerApplication extends Application {

    private String filePath = "";

    public RestFileServerApplication(String path){
       filePath = path ;
    }

      @Override
            public Restlet createInboundRoot() {
                // getConnectorService().getClientProtocols().add(Protocol.CLAP);
                getConnectorService().getServerProtocols().add(Protocol.HTTP);

                final Directory directory = new Directory(getContext(),
                        filePath);
                directory.setListingAllowed(true);
                directory.setDeeplyAccessible(true);
                directory.setNegotiatingContent(true);
                return directory;
            }

}
