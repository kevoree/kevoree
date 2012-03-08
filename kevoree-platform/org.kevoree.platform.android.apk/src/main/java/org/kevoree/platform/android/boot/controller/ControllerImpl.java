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

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import org.kevoree.platform.android.boot.KevoreeService;
import org.kevoree.platform.android.boot.kcl.TinyKCL;
import org.kevoree.platform.android.boot.view.ManagerUI;

import java.io.File;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 15:09
 */

public class ControllerImpl implements IController  {

    private static final String TAG = ControllerImpl.class.getSimpleName();

    private HandlerThread workerThread=null;
    private Handler workerHandler=null;
    private KController kController=null;
    public static ManagerUI viewmanager=null;
    public static TinyKCL tkcl = null;
    private FragmentActivity ctx=null;


    public ControllerImpl(KController _kController)
    {
        this.kController = _kController;
        viewmanager = _kController.getViewManager();
        this.ctx  = viewmanager.getCtx();
        workerThread = new HandlerThread("Worker Thread");
        workerThread.start();
        workerHandler = new Handler(workerThread.getLooper());
        initKCL();
    }

    @Override
    public boolean handleMessage(Request req) {
        switch(req)
        {
            case KEVOREE_START:
                Log.i(TAG, "KEVOREE_START");

                Intent intent_start = new Intent(viewmanager.getCtx(), KevoreeService.class);
                intent_start.putExtra("nodeName", "node0");
                viewmanager.getCtx().startService(intent_start);

                break;

            case KEVOREE_STOP:
                Log.i(TAG, "KEVOREE_STOP");


                break;
        }
        return false;
    }

    @Override
    public boolean handleMessage(Request what, Object data) {
        return false;
    }

    @Override
    public boolean handleMessage(Request req, final String key, final View view) {

        switch(req)
        {
            case ADD_TO_GROUP:
                Log.i(TAG, "MESSAGE_ADD_TO_GROUP");
                workerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewmanager.addToGroup(key, view);
                    }
                });

                break;

            case REMOVE_VIEW:
                Log.i(TAG, "MESSAGE_REMOVE_VIEW");
                workerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewmanager.removeView(view);
                    }
                });

                break;
        }
        return false;
    }

    @Override
    public boolean restoreIU() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public ManagerUI getViewManager(){
        return viewmanager;
    }

    public FragmentActivity getCtx() {
        return ctx;
    }

    public void setCtx(FragmentActivity ctx) {
        this.ctx = ctx;
    }

    public void initKCL()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                tkcl = new TinyKCL();
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
                tkcl.start(ctx.getBaseContext(), ctx.getClassLoader());
            }
        }).start();
    }



}