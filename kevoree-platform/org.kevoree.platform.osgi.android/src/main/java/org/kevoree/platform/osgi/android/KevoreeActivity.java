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
package org.kevoree.platform.osgi.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.TabHost.TabSpec;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.platform.osgi.android.ui.PreExistingViewFactory;

/**
 * Hello world!
 */
public class KevoreeActivity extends Activity implements KevoreeAndroidService {

    public static KevoreeAndroidService last = null;

    public static String nodeName = "";

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("kevoree", "Kevoree UIActivity Start /" + this.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("kevoree", "Kevoree UIActivity Stop /" + this.toString());
        // Intent intent = new Intent(".AndroidFelixService.ACTION");
        //stopService(intent);
    }

    private Boolean alreadyStarted = false;
    private TabHost tabs = null;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        last = this;
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        setContentView(main);
        tabs = new TabHost(this);
        tabs.setId(android.R.id.tabhost);
        main.addView(tabs);
        TabWidget tabWidget = new TabWidget(this);
        tabWidget.setId(android.R.id.tabs);
        tabs.addView(tabWidget);
        FrameLayout tabContent = new FrameLayout(this);
        tabContent.setId(android.R.id.tabcontent);
        tabContent.setPadding(0, 65, 0, 0);
        tabs.addView(tabContent);
        tabs.setup();

        TabSpec tspec1 = tabs.newTabSpec("Admin");
        tspec1.setIndicator("Admin", this.getResources().getDrawable(android.R.drawable.ic_menu_preferences));

        LinearLayout adminLayout = new LinearLayout(this);


        final EditText nodeNameView = new EditText(this);
        nodeNameView.setText("dukeTab");
        nodeNameView.setWidth(200);

        Button btstart = new Button(this);
        btstart.setText("Start");
        Button btstop = new Button(this);
        btstop.setText("Stop");

        adminLayout.addView(nodeNameView);
        adminLayout.addView(btstart);
        adminLayout.addView(btstop);

        tspec1.setContent(new PreExistingViewFactory(adminLayout));
        tabs.addTab(tspec1);
        /*
        TabSpec tspec2 = tabs.newTabSpec("Tab2");
        tspec2.setIndicator("Two", this.getResources().getDrawable(android.R.drawable.star_on));
        tspec2.setContent(new PreExistingViewFactory(content2));

        tabs.addTab(tspec2);
        TabSpec tspec3 = tabs.newTabSpec("Tab3");
        tspec3.setIndicator("Three", this.getResources().getDrawable(android.R.drawable.star_on));
        tspec3.setContent(new PreExistingViewFactory(content3));
        tabs.addTab(tspec3);


        this.setContentView(R.layout.main);
         */

        final Context app_ctx = this.getApplicationContext();

        final Context ctx = this;

        //   Button btstart = (Button) findViewById(R.id.StartFelix);
        btstart.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
                Intent intent_start = new Intent(ctx, AndroidFelixService.class);
                Log.i("art2.service", "start bind service");
                if (!alreadyStarted) {
                    nodeName=  nodeNameView.getText().toString();

                    startService(intent_start);
                    alreadyStarted = true;
                }

                // Toast.makeText(ctx, "Art2 Platform Started !", 3000).show();


                /*
                bindService(intent_start, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                AndroidFelixServiceBinder felixservicebinder = (AndroidFelixServiceBinder) service;
                BundleContext ctx = felixservicebinder.getService().getFrameworkBundleContext();
                //Toast.makeText(app_ctx, "size=" + ctx.getBundles().length, 3000).show();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                //throw new UnsupportedOperationException("Not supported yet.");
                }
                }, BIND_AUTO_CREATE);
                 */
                //startService(intent_start);

            }
        });

        //  Button btstop = (Button) findViewById(R.id.StopFelix);
        btstop.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
                Log.i("art2.platform", "try to stop the platform");
                if (alreadyStarted) {
                    Intent intent_stop = new Intent(ctx, AndroidFelixService.class);
                    stopService(intent_stop);
                    alreadyStarted = false;
                }
            }
        });


    }

    @Override
    public Activity getRootActivity() {
        return this;
    }

    @Override
    public void addToGroup(final String groupKey, final View view) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TabSpec tspec3 = tabs.newTabSpec(groupKey);
                tspec3.setIndicator(groupKey, getResources().getDrawable(android.R.drawable.star_on));
                tspec3.setContent(new PreExistingViewFactory(view));
                tabs.addTab(tspec3);
            }
        });

    }

    @Override
    public void remove(View view) {

    }
}
