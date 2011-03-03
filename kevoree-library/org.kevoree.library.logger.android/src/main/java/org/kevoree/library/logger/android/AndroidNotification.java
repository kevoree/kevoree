/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.logger.android;

import android.widget.TextView;
import android.widget.Toast;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;

/**
 *
 * @author ffouquet
 */
@Provides({
    @ProvidedPort(name = "log", type = PortType.MESSAGE)
})
@Library(name = "Kevoree-Android-JavaSE")
@ComponentType
public class AndroidNotification extends AbstractComponentType {

    KevoreeAndroidService uiService = null;
    TextView view = null;

    @Start
    public void start() {
        System.out.println("Hello ART2 Android v2.0 !!! ");

        KevoreeAndroidService uiService = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle"));


        view = new TextView(uiService.getRootActivity());

        uiService.addToGroup("log", view);

    }

    @Stop
    public void stop() {
        System.out.println("Bye ART2 Android !!! ");
    }

    @Port(name = "log")
    public void triggerLog(final Object logMsg) {
        uiService.getRootActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(uiService.getRootActivity(), "log="+logMsg.toString(), 2000);
            }
        });
    }
}
