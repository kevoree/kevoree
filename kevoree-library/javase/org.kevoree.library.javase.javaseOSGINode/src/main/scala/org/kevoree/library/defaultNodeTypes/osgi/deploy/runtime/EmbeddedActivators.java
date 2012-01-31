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

package org.kevoree.library.defaultNodeTypes.osgi.deploy.runtime;

import org.kevoree.library.defaultNodeTypes.osgi.deploy.runtime.shell.ShellActivator;
import org.osgi.framework.BundleActivator;

import java.util.Arrays;
import java.util.List;

/**
 * @author ffouquet
 */
public class EmbeddedActivators {

    static BundleActivator shellA = new ShellActivator();
    private static List<BundleActivator> activators = Arrays.asList(
            shellA,
            (BundleActivator)new org.apache.felix.shell.tui.Activator()
            );


    public static List<BundleActivator> getActivators() {
        return activators;
    }


    public static void setActivators(List<BundleActivator> newActs) {
        activators = newActs;
    }

}
