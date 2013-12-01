/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
package org.kevoree.platform.android.boot.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import org.kevoree.android.framework.service.events.EventListenerList;
import org.kevoree.android.framework.service.events.IntentListener;
import org.kevoree.platform.android.boot.KevoreeService;
import org.kevoree.platform.android.boot.view.ManagerUI;

import java.io.File;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 15:09
 */

public class ControllerImpl implements IController {

    private static final String TAG = ControllerImpl.class.getSimpleName();
    private ManagerUI viewmanager = null;
    private FragmentActivity ctx = null;
    private EventListenerList intentListener = new EventListenerList();


    public ControllerImpl(FragmentActivity act) {
        viewmanager = new ManagerUI(act);
        this.ctx = viewmanager.getCtx();

    }



    @Override
    public boolean handleMessage(Request req) {
        switch (req) {
            case KEVOREE_STOP:
                Log.i(TAG, "KEVOREE_STOP");
                Intent intent_stop = new Intent(ctx, KevoreeService.class);
                if(ctx.stopService(intent_stop)){
                    Log.i(TAG, "Kevoree is stopping");
                } else {
                    Log.e(TAG, "Error while stopping");
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;

            default:
                Log.e(TAG, "THE Controller can't handle this message " + req);
                break;
        }
        return false;
    }

    @Override
    public boolean handleMessage(Request req, final Object data) {
        try {
            switch (req) {
                case KEVOREE_START:
                    Log.i(TAG, "KEVOREE_START");
                    Intent intent_start = new Intent(ctx, KevoreeService.class);

                    List<String> datas = (List<String>) data;
                    String nodeName = datas.get(0);
                    String groupName = datas.get(1);
                    intent_start.putExtra("nodeName", nodeName);
                    intent_start.putExtra("groupName", groupName);
                    ctx.startService(intent_start);
                    break;

                case REMOVE_VIEW:
                    Log.i(TAG, "MESSAGE_REMOVE_VIEW");
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View viewtoremove = (View) data;
                            viewmanager.removeView(viewtoremove);

                        }
                    });

                    break;

                case  INTENT_FORWARD:
                    Object[] listeners = intentListener.getListenerList();
                    for (int i = 0; i < listeners.length; i += 2)
                    {
                        ((IntentListener) listeners[i + 1]).onNewIntent((Intent) data);
                    }
                    break;

                default:
                    Log.e(TAG, "THE Controller can't handle this message " + req);
                    break;


            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean handleMessage(Request req, final String key, final View view) {
        try {
            switch (req) {
                case ADD_TO_GROUP:
                    Log.i(TAG, "MESSAGE_ADD_TO_GROUP");
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewmanager.addToGroup(key, view);
                        }
                    });

                    break;
                default:
                    Log.e(TAG, "THE Controller can't handle this message " + req);
                    break;


            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public ManagerUI getViewManager() {
        return viewmanager;
    }

    public FragmentActivity getCtx() {
        return viewmanager.getCtx();
    }

    public void setCtx(FragmentActivity ctx) {
        this.ctx = ctx;
    }


    public static void initKCL(Context c) {
        File sdDir = Environment.getExternalStorageDirectory();
        File kevoree_cache = new File(sdDir.getAbsolutePath() + "/KEVOREE");
        Log.i("kevoree.android", kevoree_cache.getAbsolutePath());
        if (!kevoree_cache.exists()) {
            if (!kevoree_cache.mkdirs()) {
                Log.e("kevoree.M2", "unable to create cache");
                throw new IllegalStateException("Unable to create kevoree maven repo cache dir");
            } else {
                Log.i("kevoree.M2", "cache created");
            }
        }
        System.setProperty("user.home", kevoree_cache.getAbsolutePath());
    }


    @Override
    public void addToGroup(String groupKey, View view) {
        handleMessage(Request.ADD_TO_GROUP, groupKey, view);
    }

    @Override
    public void removeView(View view) {
        handleMessage(Request.REMOVE_VIEW, view);
    }

    @Override
    public void addIntentListener(IntentListener listener) {
        intentListener.add(IntentListener.class,listener);
    }

    @Override
    public void removeIntentListener(IntentListener listener) {
        intentListener.remove(IntentListener.class, listener);
    }




}