package org.kevoree.library.android.nodeType;

import android.app.Activity;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.osgi.framework.Bundle;

/**
 * User: ffouquet
 * Date: 15/09/11
 * Time: 17:10
 */

@NodeType
@DictionaryType({
    @DictionaryAttribute(name = "autodiscovery", defaultValue = "true", optional = true,vals={"true","false"})
})
public class AndroidNode extends JavaSENode {

    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();

    @Override
    public void startNode() {

        if (this.getDictionary().get("autodiscovery").equals("true")) {
            Activity act = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle")).getRootActivity();
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) act.getSystemService(android.content.Context.WIFI_SERVICE);
            lock = wifi.createMulticastLock("kevoreeWifiLock");
            lock.setReferenceCounted(true);
            lock.acquire();

            handler.postDelayed(new Runnable() {
                public void run() {
                    callSuperNode();
                }
            }, 1000);
        }


    }

    private void callSuperNode() {
        super.startNode();
    }

    @Override
    public void stopNode() {
        if (this.getDictionary().get("autodiscovery").equals("true")) {
            super.stopNode();
            lock.release();
        }

    }
}
