package org.kevoree.android.framework.service.events;

import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 27/05/13
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface IntentListener extends java.util.EventListener {
    void onNewIntent(Intent intent);
}
