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
package org.kevoree.platform.android.boot.view;

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.kevoree.platform.android.boot.utils.KObservable;
import org.kevoree.platform.android.ui.KevoreeAndroidUIScreen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 15:10
 */
public class ManagerUI extends KObservable<ManagerUI> implements KevoreeAndroidUIScreen,ActionBar.TabListener {

    private static final String TAG = ManagerUI.class.getSimpleName();
    private Map<ActionBar.Tab, LinearLayout> views = new HashMap<ActionBar.Tab, LinearLayout>();
    private FragmentActivity ctx=null;

    public ManagerUI(FragmentActivity context){
        this.ctx = context;
        ctx.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    public void restoreViews(FragmentActivity newctx)
    {
        ctx.getSupportActionBar().removeAllTabs();
        newctx.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ctx = newctx;
        Map<ActionBar.Tab, LinearLayout> backup = new HashMap<ActionBar.Tab, LinearLayout>();
        backup.putAll(views);
        views.clear();
        Iterator it = backup.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry pairs = (Map.Entry)it.next();
            ActionBar.Tab tab =( ActionBar.Tab)pairs.getKey();
            LinearLayout layout =  (LinearLayout)   pairs.getValue() ;
            Log.i(TAG,"Restore "+tab.getText());

            // if exist remove parent
            if(layout.getParent() != null)
            {
                ((ViewGroup) layout.getParent()).removeView(layout);
            }
            addToGroup(tab.getText().toString(),layout);
        }
    }


    @Override
    public void addToGroup(String groupKey, View view)
    {
        ActionBar.Tab idTab = getTabById(groupKey);
        Log.i("KevoreeBoot", "Add" + groupKey + "-" + idTab + "-" + view);

        if (idTab == null) {
            idTab = ctx.getSupportActionBar().newTab();
            idTab.setText(groupKey);
            idTab.setTabListener(this);
            ctx.getSupportActionBar().addTab(idTab);
            LinearLayout tabLayout = new LinearLayout(ctx);
            idTab.setCustomView(tabLayout);
            views.put(idTab, tabLayout);
        }
        views.get(idTab).addView(view);

        /// Set the screen content to an the groupkey
        ctx.setContentView(views.get(getTabById(groupKey)));
        notifyObservers(this);
    }


    @Override
    public void removeView(View view)
    {
        for (ActionBar.Tab idTab : views.keySet()) {
            if (idTab != null) {
                LinearLayout l = views.get(idTab);
                l.removeView(view);
                if (l.getChildCount() == 0) {
                    ctx.getSupportActionBar().removeTab(idTab);
                    views.remove(idTab);
                }
            }
        }
        notifyObservers(this);
    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public ActionBar.Tab getTabById(String id) {
        for (ActionBar.Tab t : views.keySet()) {
            if (t.getText().equals(id)) {
                return t;
            }
        }
        return null;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        LinearLayout l = views.get(tab);
        if(l != null){
            ctx.setContentView(l);
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public FragmentActivity getCtx() {
        return ctx;
    }
}