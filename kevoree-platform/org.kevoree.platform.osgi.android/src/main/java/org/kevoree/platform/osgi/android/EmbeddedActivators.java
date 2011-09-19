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

package org.kevoree.platform.osgi.android;

import java.util.Arrays;
import java.util.List;

import org.kevoree.ContainerRoot;
import org.osgi.framework.BundleActivator;

/**
 *
 * @author ffouquet
 */
public class EmbeddedActivators {

    public static List<BundleActivator> getActivators(ContainerRoot model){
        BootstrapActivator bact = new BootstrapActivator();
        bact.setBootstrapModel(model);
        return Arrays.asList(
                (BundleActivator)new org.apache.felix.shell.impl.Activator(),
              //  (BundleActivator)new org.apache.felix.shell.tui.Activator(),
                (BundleActivator)new org.ops4j.pax.url.assembly.internal.Activator(),
                bact
                );
    }

}
