/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.logger.android;

import android.widget.TextView;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;

/**
 * @author ffouquet
 */
@Provides({
        @ProvidedPort(name = "log", type = PortType.MESSAGE)
})
@Library(name = "Kevoree-Android")
@ComponentType
public class AndroidNotification extends AbstractComponentType {

    KevoreeAndroidService uiService = null;
    TextView view = null;

    @Start
    public void start() {
        uiService = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle"));
        view = new TextView(uiService.getRootActivity());
        uiService.addToGroup("kevlog2", view);
    }

    @Stop
    public void stop() {
        uiService.remove(view);
    }

    @Port(name = "log")
    public void triggerLog(final Object logMsg) {
        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(view.getText() + "\n" + logMsg);
            }
        });
    }
}
