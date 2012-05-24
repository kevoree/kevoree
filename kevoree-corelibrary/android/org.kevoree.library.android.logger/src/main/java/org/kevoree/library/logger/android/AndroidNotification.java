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

/**
 * @author ffouquet
 */
@Provides({
        @ProvidedPort(name = "log", type = PortType.MESSAGE)
})
@Library(name = "Android")
@ComponentType
public class AndroidNotification extends AbstractComponentType {

    KevoreeAndroidService uiService = null;
    TextView view = null;

    @Start
    public void start() {
        uiService = UIServiceHandler.getUIService();        //view = new TextView(uiService.getRootActivity());
        view = new  TextView(uiService.getRootActivity());
        uiService.addToGroup("kevlog"+getName(), view);
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
