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
package org.kevoree.android.framework.helper;

import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.osgi.framework.Bundle;
import org.osgi.util.tracker.ServiceTracker;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ffouquet
 */
public class UIServiceHandler {

    public static KevoreeAndroidService getUIService(Bundle b) {
        ServiceTracker tracker = new ServiceTracker(b.getBundleContext(), KevoreeAndroidService.class.getName(), null);
        tracker.open();
        KevoreeAndroidService uis = null;
        try {
            uis = (KevoreeAndroidService) tracker.waitForService(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UIServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        tracker.close();
        return uis;
    }
}
