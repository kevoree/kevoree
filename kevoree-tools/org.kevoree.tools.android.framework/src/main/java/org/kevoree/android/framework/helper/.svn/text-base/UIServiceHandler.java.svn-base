/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kermeta.art2.android.framework.helper;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.kermeta.art2.android.framework.service.Art2AndroidUI;
import org.osgi.framework.Bundle;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 * @author ffouquet
 */
public class UIServiceHandler {

    public static Art2AndroidUI getUIService(Bundle b) {
        ServiceTracker tracker = new ServiceTracker(b.getBundleContext(), Art2AndroidUI.class.getName(), null);
        tracker.open();
        Art2AndroidUI uis = null;
        try {
            uis = (Art2AndroidUI) tracker.waitForService(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UIServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        tracker.close();
        return uis;
    }
}
